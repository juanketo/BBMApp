package org.example.appbbmges.ui.usuarios.viewusuarios.viewpagos

import org.example.appbbmges.data.Repository
import org.example.appbbmges.PrecioBaseEntity
import org.example.appbbmges.MembershipEntity
import org.example.appbbmges.InscriptionEntity

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

// Data class para información de membresía
data class MembershipInfo(
    val id: Long,
    val name: String,
    val monthsPaid: Long,
    val monthsSaved: Double,
    val totalPrice: Double,
    val membershipType: String // "BRONCE", "PLATA", "ORO", "PLATINO"
)

// Data class para información de inscripción
data class InscriptionInfo(
    val id: Long,
    val precio: Double
)

// Data class para el resultado del cálculo (COMPLETO Y EXPANDIDO)
data class PaymentResult(
    val baseAmount: Double,
    val discount: Double,
    val finalAmount: Double,
    val description: String,
    val breakdown: String,
    val membershipInfo: MembershipInfo? = null,

    // NUEVOS CAMPOS ESPECÍFICOS PARA BABY BALLET:
    val membershipType: String? = null,          // "BRONCE", "PLATA", "ORO", "PLATINO"
    val discountType: String? = null,            // "50% 2da disciplina", "10% hermanos", "15% hermanos"
    val paymentTiming: PaymentTiming,            // NORMAL, LATE_ACTIVE, PROPORTIONAL_NEW, MONTH_END
    val includesEnrollment: Boolean,             // Si incluye inscripción
    val enrollmentFee: Double,                   // Precio de inscripción desde BD
    val taxBase: Double,                         // Base imponible (para IVA)
    val taxAmount: Double,                       // IVA (16%)
    val taxPercentage: Double = 16.0,            // 16% fijo
    val subtotalBeforeTax: Double,               // Subtotal sin IVA
    val disciplinesCount: Int,                   // 1, 2, 3, 4 disciplinas
    val siblingsCount: Int,                      // 1, 2, 3 hermanos
    val frequency: String,                       // "2 veces/semana", "4 veces/semana", etc.
    val duration: String = "Mes",                // "Mes" (por defecto)
    val selectedMonths: List<String> = emptyList(), // ["oct. 2025"]
    val paymentMethod: String = "Efectivo",      // "Efectivo", "Transferencia", "Tarjeta"
    val isMembership: Boolean = false,           // Si es pago de membresía
    val monthsPaid: Int = 1,                     // Cantidad de meses pagados
    val monthsSaved: Int = 0                     // Meses ahorrados con membresía
)

// Interface para el repository (para hacer la calculadora testeable)
interface PaymentRepository {
    fun getAllPreciosBase(): List<PrecioBaseEntity>
    fun getPrecioBaseById(id: Long): PrecioBaseEntity?
    fun getAllMemberships(): List<MembershipEntity>
    fun getMembershipById(id: Long): MembershipEntity?
    fun getAllInscriptions(): List<InscriptionEntity>
    fun getInscriptionById(id: Long): InscriptionEntity?
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
        inscriptionId: Long = 1L, // ID de la inscripción a usar
        timing: PaymentTiming = PaymentTiming.NORMAL,
        includeEnrollment: Boolean = false,
        paymentMethod: String = "Efectivo"
    ): PaymentResult {

        // Obtener precio base de la base de datos
        val precioBase = repository.getPrecioBaseById(precioBaseId)
            ?: throw IllegalArgumentException("No se encontró precio base con ID: $precioBaseId")

        // Obtener precio de inscripción de la base de datos
        val inscription = repository.getInscriptionById(inscriptionId)
            ?: throw IllegalArgumentException("No se encontró inscripción con ID: $inscriptionId")

        val basePrice = precioBase.precio
        val enrollmentFee = inscription.precio

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

        // Calcular impuestos (IVA 16%)
        val (taxBase, taxAmount) = calculateTaxes(finalAmount)

        val enrollmentText = if (includeEnrollment) " + Inscripción (\$${enrollmentFee.toInt()})" else ""
        val timingText = if (timing != PaymentTiming.NORMAL && selection !is PaymentSelection.Membership) {
            " [${timing.description}]"
        } else ""

        // Determinar frecuencia basada en disciplinas
        val frequency = when (selection) {
            is PaymentSelection.Disciplines -> "${selection.count * 2} veces/semana"
            is PaymentSelection.Membership -> "Membresía ${baseResult.membershipInfo?.membershipType ?: ""}"
            else -> "Personalizado"
        }

        return PaymentResult(
            baseAmount = baseResult.baseAmount,
            discount = baseResult.discount,
            finalAmount = finalAmount,
            description = baseResult.description + timingText + enrollmentText,
            breakdown = baseResult.breakdown +
                    (if (timing != PaymentTiming.NORMAL && selection !is PaymentSelection.Membership)
                        "\nAjuste por timing: ${timing.description}" else "") +
                    (if (includeEnrollment) "\nInscripción: \$${enrollmentFee.toInt()}" else ""),
            membershipInfo = baseResult.membershipInfo,

            // NUEVOS CAMPOS:
            membershipType = baseResult.membershipInfo?.membershipType,
            discountType = baseResult.discountType,
            paymentTiming = timing,
            includesEnrollment = includeEnrollment,
            enrollmentFee = enrollmentFee,
            taxBase = taxBase,
            taxAmount = taxAmount,
            subtotalBeforeTax = finalAmount - taxAmount,
            disciplinesCount = if (selection is PaymentSelection.Disciplines) selection.count else 0,
            siblingsCount = if (selection is PaymentSelection.Disciplines) selection.siblings else 1,
            frequency = frequency,
            paymentMethod = paymentMethod,
            isMembership = selection is PaymentSelection.Membership,
            monthsPaid = baseResult.membershipInfo?.monthsPaid?.toInt() ?: 1,
            monthsSaved = baseResult.membershipInfo?.monthsSaved?.toInt() ?: 0
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
            val totalPrice = (membership.months_paid * basePrice) - (membership.months_saved * basePrice)

            // Determinar tipo de membresía basado en meses pagados
            val membershipType = when (membership.months_paid) {
                4L -> "BRONCE"
                6L -> "PLATA"
                10L -> "ORO"
                12L -> "PLATINO"
                else -> "PERSONALIZADA"
            }

            MembershipInfo(
                id = membership.id,
                name = membership.name,
                monthsPaid = membership.months_paid,
                monthsSaved = membership.months_saved,
                totalPrice = totalPrice,
                membershipType = membershipType
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
     * Obtiene el precio de inscripción actual
     */
    fun getCurrentInscriptionPrice(inscriptionId: Long = 1L): Double {
        val inscription = repository.getInscriptionById(inscriptionId)
            ?: throw IllegalArgumentException("No se encontró inscripción con ID: $inscriptionId")
        return inscription.precio
    }

    /**
     * Obtiene todas las inscripciones disponibles
     */
    fun getAvailableInscriptions(): List<InscriptionInfo> {
        val inscriptions = repository.getAllInscriptions()
        return inscriptions.map { inscription ->
            InscriptionInfo(
                id = inscription.id,
                precio = inscription.precio
            )
        }
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
                    breakdown = "Precio base: \$${basePrice.toInt()}",
                    discountType = null,
                    paymentTiming = PaymentTiming.NORMAL,
                    includesEnrollment = false,
                    enrollmentFee = 0.0, // Se establecerá en el método principal
                    taxBase = calculateTaxes(basePrice).first,
                    taxAmount = calculateTaxes(basePrice).second,
                    subtotalBeforeTax = basePrice,
                    disciplinesCount = 1,
                    siblingsCount = 1,
                    frequency = "2 veces/semana"
                )
            }

            else -> {

                PaymentResult(
                    baseAmount = 0.0,
                    discount = 0.0,
                    finalAmount = 0.0,
                    description = "Configuración inválida",
                    breakdown = "Use SiblingsWithMixedDisciplines para este caso",
                    discountType = null,
                    paymentTiming = PaymentTiming.NORMAL,
                    includesEnrollment = false,
                    enrollmentFee = 0.0,
                    taxBase = 0.0,
                    taxAmount = 0.0,
                    subtotalBeforeTax = 0.0,
                    disciplinesCount = 0,
                    siblingsCount = 0,
                    frequency = ""
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
        val discountType = when (disciplineCount) {
            2 -> "50% 2da disciplina"
            3 -> "50% 2da + 25% 3ra disciplina"
            4 -> "50% 2da + 25% 3ra + 25% 4ta disciplina"
            else -> null
        }

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
            },
            discountType = discountType,
            paymentTiming = PaymentTiming.NORMAL,
            includesEnrollment = false,
            enrollmentFee = 0.0,
            taxBase = calculateTaxes(finalAmount).first,
            taxAmount = calculateTaxes(finalAmount).second,
            subtotalBeforeTax = finalAmount,
            disciplinesCount = disciplineCount,
            siblingsCount = 1,
            frequency = "${disciplineCount * 2} veces/semana"
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
        val discountType = when (siblingsCount) {
            2 -> "10% descuento hermanos"
            3 -> "15% descuento hermanos"
            else -> null
        }

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
            },
            discountType = discountType,
            paymentTiming = PaymentTiming.NORMAL,
            includesEnrollment = false,
            enrollmentFee = 0.0,
            taxBase = calculateTaxes(finalAmount).first,
            taxAmount = calculateTaxes(finalAmount).second,
            subtotalBeforeTax = finalAmount,
            disciplinesCount = 1,
            siblingsCount = siblingsCount,
            frequency = "2 veces/semana"
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
            breakdown = breakdown.toString() + "Total: \$${totalAmount.toInt()}",
            discountType = null,
            paymentTiming = PaymentTiming.NORMAL,
            includesEnrollment = false,
            enrollmentFee = 0.0,
            taxBase = calculateTaxes(totalAmount).first,
            taxAmount = calculateTaxes(totalAmount).second,
            subtotalBeforeTax = totalAmount,
            disciplinesCount = disciplinesPerSibling.sum(),
            siblingsCount = disciplinesPerSibling.size,
            frequency = "Variable"
        )
    }

    /**
     * Calcula precio para membresías (dinámico desde BD)
     */
    private fun calculateMembership(selection: PaymentSelection.Membership, basePrice: Double): PaymentResult {
        val membership = repository.getMembershipById(selection.membershipId)
            ?: throw IllegalArgumentException("No se encontró membresía con ID: ${selection.membershipId}")

        val baseAmount = membership.months_paid * basePrice
        val discount = membership.months_saved * basePrice
        val finalAmount = baseAmount - discount

        // Determinar tipo de membresía
        val membershipType = when (membership.months_paid) {
            4L -> "BRONCE"
            6L -> "PLATA"
            10L -> "ORO"
            12L -> "PLATINO"
            else -> "PERSONALIZADA"
        }

        val membershipInfo = MembershipInfo(
            id = membership.id,
            name = membership.name,
            monthsPaid = membership.months_paid,
            monthsSaved = membership.months_saved,
            totalPrice = finalAmount,
            membershipType = membershipType
        )

        return PaymentResult(
            baseAmount = baseAmount,
            discount = discount,
            finalAmount = finalAmount,
            description = "Membresía ${membership.name}",
            breakdown = buildString {
                append("Precio sin membresía (${membership.months_paid} meses): \$${baseAmount.toInt()}")
                append("\nAhorro (${membership.months_saved} meses): -\$${discount.toInt()}")
                append("\nTotal membresía: \$${finalAmount.toInt()}")
            },
            membershipInfo = membershipInfo,
            membershipType = membershipType,
            discountType = "Membresía $membershipType",
            paymentTiming = PaymentTiming.NORMAL,
            includesEnrollment = false,
            enrollmentFee = 0.0,
            taxBase = calculateTaxes(finalAmount).first,
            taxAmount = calculateTaxes(finalAmount).second,
            subtotalBeforeTax = finalAmount,
            disciplinesCount = 0,
            siblingsCount = 1,
            frequency = "Membresía",
            isMembership = true,
            monthsPaid = membership.months_paid.toInt(),
            monthsSaved = membership.months_saved.toInt()
        )
    }

    /**
     * Calcula impuestos (IVA 16%)
     */
    private fun calculateTaxes(amount: Double): Pair<Double, Double> {
        val taxPercentage = 0.16 // 16% IVA
        val taxBase = amount / (1 + taxPercentage)
        val taxAmount = amount - taxBase
        return Pair(taxBase, taxAmount)
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
        inscriptionId: Long = 1L,
        timing: PaymentTiming = PaymentTiming.NORMAL,
        includeEnrollment: Boolean = false,
        paymentMethod: String = "Efectivo"
    ): PaymentResult {
        val selection = PaymentSelection.Disciplines(disciplinesCount, siblingsCount)
        return calculatePayment(selection, precioBaseId, inscriptionId, timing, includeEnrollment, paymentMethod)
    }

    /**
     * Método de conveniencia para membresías
     */
    fun calculateMembershipQuick(
        membershipId: Long,
        precioBaseId: Long = 1L,
        inscriptionId: Long = 1L,
        includeEnrollment: Boolean = false,
        paymentMethod: String = "Efectivo"
    ): PaymentResult {
        val selection = PaymentSelection.Membership(membershipId)
        return calculatePayment(selection, precioBaseId, inscriptionId, includeEnrollment = includeEnrollment, paymentMethod = paymentMethod)
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

    override fun getAllInscriptions(): List<InscriptionEntity> {
        return repository.getAllInscriptions()
    }

    override fun getInscriptionById(id: Long): InscriptionEntity? {
        return repository.getInscriptionById(id)
    }
}