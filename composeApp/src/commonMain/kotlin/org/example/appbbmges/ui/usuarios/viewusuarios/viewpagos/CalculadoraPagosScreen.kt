package org.example.appbbmges.ui.usuarios.viewusuarios.viewpagos

import org.example.appbbmges.data.Repository
import org.example.appbbmges.PrecioBaseEntity
import org.example.appbbmges.MembershipEntity

// Sealed classes para definir los tipos de selección
sealed class PaymentSelection {
    // Selección de disciplinas individuales
    data class Disciplines(
        val count: Int,
        val siblings: Int = 1
    ) : PaymentSelection()

    // Selección de membresías (ahora usando ID de base de datos)
    data class Membership(
        val membershipId: Long
    ) : PaymentSelection()

    // Caso especial: hermanos con diferentes cantidades de disciplinas
    data class SiblingsWithMixedDisciplines(
        val disciplinesPerSibling: List<Int>
    ) : PaymentSelection()
}

// Enum para ajustes por fecha de pago
enum class PaymentTiming(val multiplier: Double, val description: String) {
    NORMAL(1.0, "Pago del 1-10 del mes"),
    LATE_ACTIVE(1.1, "Pago después del 10 (activos)"),
    PROPORTIONAL_NEW(1.0, "Pago proporcional (nuevos)"),
    MONTH_END(1.0, "Pago después del 20 (ajuste por clases restantes)")
}

// Data class para el resultado del cálculo
data class PaymentResult(
    val baseAmount: Double,
    val discount: Double,
    val finalAmount: Double,
    val description: String,
    val breakdown: String,
    val membershipInfo: MembershipInfo? = null
)

// Data class para información de membresía
data class MembershipInfo(
    val id: Long,
    val name: String,
    val monthsPaid: Long,
    val monthsSaved: Double,
    val totalPrice: Double
)

// Interface para el repository (para hacer la calculadora testeable)
interface PaymentRepository {
    fun getAllPreciosBase(): List<PrecioBaseEntity>
    fun getPrecioBaseById(id: Long): PrecioBaseEntity?
    fun getAllMemberships(): List<MembershipEntity>
    fun getMembershipById(id: Long): MembershipEntity?
}

class PaymentCalculator(
    private val repository: PaymentRepository
) {

    /**
     * Método principal para calcular pagos
     */
    fun calculatePayment(
        selection: PaymentSelection,
        precioBaseId: Long = 1L, // ID del precio base a usar
        timing: PaymentTiming = PaymentTiming.NORMAL,
        enrollmentFee: Double = 800.0,
        includeEnrollment: Boolean = false
    ): PaymentResult {

        // Obtener precio base de la base de datos
        val precioBase = repository.getPrecioBaseById(precioBaseId)
            ?: throw IllegalArgumentException("No se encontró precio base con ID: $precioBaseId")

        val basePrice = precioBase.precio

        val baseResult = when (selection) {
            is PaymentSelection.Disciplines -> calculateDisciplines(selection, basePrice)
            is PaymentSelection.Membership -> calculateMembership(selection, basePrice)
            is PaymentSelection.SiblingsWithMixedDisciplines -> calculateSiblingsWithMixed(selection, basePrice)
        }

        // Aplicar ajuste por timing (solo para disciplinas, no membresías)
        val adjustedAmount = if (selection is PaymentSelection.Membership) {
            baseResult.finalAmount
        } else {
            applyTimingAdjustment(baseResult.finalAmount, timing)
        }

        // Agregar inscripción si es necesario
        val finalAmount = if (includeEnrollment) {
            adjustedAmount + enrollmentFee
        } else {
            adjustedAmount
        }

        val enrollmentText = if (includeEnrollment) " + Inscripción (\$${enrollmentFee.toInt()})" else ""
        val timingText = if (timing != PaymentTiming.NORMAL && selection !is PaymentSelection.Membership) {
            " [${timing.description}]"
        } else ""

        return baseResult.copy(
            finalAmount = finalAmount,
            description = baseResult.description + timingText + enrollmentText,
            breakdown = baseResult.breakdown +
                    (if (timing != PaymentTiming.NORMAL && selection !is PaymentSelection.Membership)
                        "\nAjuste por timing: ${timing.description}" else "") +
                    (if (includeEnrollment) "\nInscripción: \$${enrollmentFee.toInt()}" else "")
        )
    }

    /**
     * Obtiene todas las membresías disponibles con sus precios calculados
     */
    fun getAvailableMemberships(precioBaseId: Long = 1L): List<MembershipInfo> {
        val precioBase = repository.getPrecioBaseById(precioBaseId)
            ?: throw IllegalArgumentException("No se encontró precio base con ID: $precioBaseId")

        val basePrice = precioBase.precio
        val memberships = repository.getAllMemberships()

        return memberships.map { membership ->
            // CORRECCIÓN: Usar nombres correctos de propiedades
            val totalPrice = (membership.monthsPaid * basePrice) - (membership.monthsSaved * basePrice)
            MembershipInfo(
                id = membership.id,
                name = membership.name,
                monthsPaid = membership.monthsPaid,
                monthsSaved = membership.monthsSaved,
                totalPrice = totalPrice
            )
        }
    }

    /**
     * Obtiene el precio base actual
     */
    fun getCurrentBasePrice(precioBaseId: Long = 1L): Double {
        val precioBase = repository.getPrecioBaseById(precioBaseId)
            ?: throw IllegalArgumentException("No se encontró precio base con ID: $precioBaseId")
        return precioBase.precio
    }

    /**
     * Calcula el precio para disciplinas individuales
     */
    private fun calculateDisciplines(selection: PaymentSelection.Disciplines, basePrice: Double): PaymentResult {
        val disciplineCount = selection.count
        val siblingsCount = selection.siblings

        return when {
            // Caso: Un solo alumno con múltiples disciplinas
            siblingsCount == 1 && disciplineCount > 1 -> {
                calculateMultipleDisciplinesDiscount(disciplineCount, basePrice)
            }

            // Caso: Hermanos con una disciplina cada uno
            siblingsCount > 1 && disciplineCount == 1 -> {
                calculateSiblingsDiscount(siblingsCount, basePrice)
            }

            // Caso: Un alumno, una disciplina (precio normal)
            siblingsCount == 1 && disciplineCount == 1 -> {
                PaymentResult(
                    baseAmount = basePrice,
                    discount = 0.0,
                    finalAmount = basePrice,
                    description = "1 disciplina",
                    breakdown = "Precio base: \$${basePrice.toInt()}"
                )
            }

            else -> {
                // Caso inválido: hermanos con múltiples disciplinas cada uno
                // Esto se debería manejar con SiblingsWithMixedDisciplines
                PaymentResult(
                    baseAmount = 0.0,
                    discount = 0.0,
                    finalAmount = 0.0,
                    description = "Configuración inválida",
                    breakdown = "Use SiblingsWithMixedDisciplines para este caso"
                )
            }
        }
    }

    /**
     * Calcula descuento por múltiples disciplinas (mismo alumno)
     */
    private fun calculateMultipleDisciplinesDiscount(disciplineCount: Int, basePrice: Double): PaymentResult {
        val baseAmount = basePrice * disciplineCount
        val discount = when (disciplineCount) {
            2 -> basePrice * 0.5 // 50% descuento en la 2da
            3 -> basePrice * 0.5 + basePrice * 0.25 // 50% + 25%
            4 -> basePrice * 0.5 + basePrice * 0.25 + basePrice * 0.25 // 50% + 25% + 25%
            else -> 0.0
        }

        val finalAmount = baseAmount - discount

        return PaymentResult(
            baseAmount = baseAmount,
            discount = discount,
            finalAmount = finalAmount,
            description = "$disciplineCount disciplinas (mismo alumno)",
            breakdown = buildString {
                append("Precio base ($disciplineCount disciplinas): \$${baseAmount.toInt()}")
                when (disciplineCount) {
                    2 -> append("\nDescuento 2da disciplina (50%): -\$${discount.toInt()}")
                    3 -> append("\nDescuento 2da disciplina (50%): -\$${(basePrice * 0.5).toInt()}")
                        .append("\nDescuento 3ra disciplina (25%): -\$${(basePrice * 0.25).toInt()}")
                    4 -> append("\nDescuento 2da disciplina (50%): -\$${(basePrice * 0.5).toInt()}")
                        .append("\nDescuento 3ra disciplina (25%): -\$${(basePrice * 0.25).toInt()}")
                        .append("\nDescuento 4ta disciplina (25%): -\$${(basePrice * 0.25).toInt()}")
                }
                append("\nTotal con descuento: \$${finalAmount.toInt()}")
            }
        )
    }

    /**
     * Calcula descuento por hermanos (una disciplina cada uno)
     */
    private fun calculateSiblingsDiscount(siblingsCount: Int, basePrice: Double): PaymentResult {
        val baseAmount = basePrice * siblingsCount
        val discountPercentage = when (siblingsCount) {
            2 -> 0.10 // 10%
            3 -> 0.15 // 15%
            else -> 0.0
        }

        val discount = baseAmount * discountPercentage
        val finalAmount = baseAmount - discount

        return PaymentResult(
            baseAmount = baseAmount,
            discount = discount,
            finalAmount = finalAmount,
            description = "$siblingsCount hermanos (1 disciplina c/u)",
            breakdown = buildString {
                append("Precio base ($siblingsCount hermanos): \$${baseAmount.toInt()}")
                if (discountPercentage > 0) {
                    append("\nDescuento hermanos (${(discountPercentage * 100).toInt()}%): -\$${discount.toInt()}")
                }
                append("\nTotal: \$${finalAmount.toInt()}")
            }
        )
    }

    /**
     * Calcula precio para hermanos con diferentes cantidades de disciplinas
     */
    private fun calculateSiblingsWithMixed(selection: PaymentSelection.SiblingsWithMixedDisciplines, basePrice: Double): PaymentResult {
        val disciplinesPerSibling = selection.disciplinesPerSibling
        var totalAmount = 0.0
        val breakdown = StringBuilder()

        disciplinesPerSibling.forEachIndexed { index, disciplines ->
            val siblingCost = if (disciplines == 1) {
                basePrice
            } else {
                val siblingSelection = PaymentSelection.Disciplines(disciplines, 1)
                calculateDisciplines(siblingSelection, basePrice).finalAmount
            }

            totalAmount += siblingCost
            breakdown.append("Hermano ${index + 1} ($disciplines disciplina${if(disciplines > 1) "s" else ""}): \$${siblingCost.toInt()}\n")
        }

        return PaymentResult(
            baseAmount = totalAmount,
            discount = 0.0, // No hay descuento global cuando hay disciplinas mixtas
            finalAmount = totalAmount,
            description = "Hermanos con disciplinas mixtas",
            breakdown = breakdown.toString() + "Total: \$${totalAmount.toInt()}"
        )
    }

    /**
     * Calcula precio para membresías (dinámico desde BD)
     */
    private fun calculateMembership(selection: PaymentSelection.Membership, basePrice: Double): PaymentResult {
        val membership = repository.getMembershipById(selection.membershipId)
            ?: throw IllegalArgumentException("No se encontró membresía con ID: ${selection.membershipId}")

        // CORRECCIÓN: Usar nombres correctos de propiedades
        val baseAmount = membership.monthsPaid * basePrice
        val discount = membership.monthsSaved * basePrice
        val finalAmount = baseAmount - discount

        val membershipInfo = MembershipInfo(
            id = membership.id,
            name = membership.name,
            monthsPaid = membership.monthsPaid,
            monthsSaved = membership.monthsSaved,
            totalPrice = finalAmount
        )

        return PaymentResult(
            baseAmount = baseAmount,
            discount = discount,
            finalAmount = finalAmount,
            description = "Membresía ${membership.name}",
            breakdown = buildString {
                append("Precio sin membresía (${membership.monthsPaid} meses): \$${baseAmount.toInt()}")
                append("\nAhorro (${membership.monthsSaved} meses): -\$${discount.toInt()}")
                append("\nTotal membresía: \$${finalAmount.toInt()}")
            },
            membershipInfo = membershipInfo
        )
    }

    private fun applyTimingAdjustment(amount: Double, timing: PaymentTiming): Double {
        return amount * timing.multiplier
    }

    /**
     * Método de conveniencia para casos comunes
     */
    fun calculateQuickSelection(
        disciplinesCount: Int,
        siblingsCount: Int = 1,
        precioBaseId: Long = 1L,
        timing: PaymentTiming = PaymentTiming.NORMAL,
        includeEnrollment: Boolean = false
    ): PaymentResult {
        val selection = PaymentSelection.Disciplines(disciplinesCount, siblingsCount)
        return calculatePayment(selection, precioBaseId, timing, includeEnrollment = includeEnrollment)
    }

    /**
     * Método de conveniencia para membresías
     */
    fun calculateMembershipQuick(
        membershipId: Long,
        precioBaseId: Long = 1L,
        includeEnrollment: Boolean = false
    ): PaymentResult {
        val selection = PaymentSelection.Membership(membershipId)
        return calculatePayment(selection, precioBaseId, includeEnrollment = includeEnrollment)
    }
}

// Implementación del repository interface para tu caso
class DatabasePaymentRepository(private val repository: Repository) : PaymentRepository {
    override fun getAllPreciosBase(): List<PrecioBaseEntity> {
        return repository.getAllPreciosBase()
    }

    override fun getPrecioBaseById(id: Long): PrecioBaseEntity? {
        return repository.getPrecioBaseById(id)
    }

    override fun getAllMemberships(): List<MembershipEntity> {
        return repository.getAllMemberships()
    }

    override fun getMembershipById(id: Long): MembershipEntity? {
        return repository.getMembershipById(id)
    }
}

// Ejemplo de uso simplificado
fun exampleUsage(repository: Repository) {
    val paymentRepository = DatabasePaymentRepository(repository)
    val calculator = PaymentCalculator(paymentRepository)

    // Obtener membresías disponibles con precios calculados
    val memberships = calculator.getAvailableMemberships(precioBaseId = 1L)
    memberships.forEach { membership ->
        println("${membership.name}: \$${membership.totalPrice.toInt()} (Paga ${membership.monthsPaid} meses, ahorra ${membership.monthsSaved} meses)")
    }

    // Calcular precio para 2 disciplinas
    val result2Disciplines = calculator.calculateQuickSelection(
        disciplinesCount = 2,
        precioBaseId = 1L
    )
    println("\n${result2Disciplines.description}: \$${result2Disciplines.finalAmount.toInt()}")

    // Calcular precio para membresía específica (usando ID de la BD)
    val membershipResult = calculator.calculateMembershipQuick(
        membershipId = 1L, // ID de la membresía en tu BD
        precioBaseId = 1L
    )
    println("\n${membershipResult.description}: \$${membershipResult.finalAmount.toInt()}")
}