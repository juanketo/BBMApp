package org.example.appbbmges.data

import kotlinx.datetime.Clock
import org.example.appbbmges.AdministrativeEntity
import org.example.appbbmges.AppDatabaseBaby
import org.example.appbbmges.BoutiqueItemEntity
import org.example.appbbmges.ClassAttendanceEntity
import org.example.appbbmges.ClassroomEntity
import org.example.appbbmges.DisciplineSelectAll
import org.example.appbbmges.EventEntity
import org.example.appbbmges.EventPaymentEntity
import org.example.appbbmges.FamilyEntity
import org.example.appbbmges.FranchiseBoutiqueInventoryEntity
import org.example.appbbmges.FranchiseDisciplineEntity
import org.example.appbbmges.FranchiseEntity
import org.example.appbbmges.FranchiseTeacherEntity
import org.example.appbbmges.InscriptionEntity
import org.example.appbbmges.LevelEntity
import org.example.appbbmges.MembershipEntity
import org.example.appbbmges.PaymentEntity
import org.example.appbbmges.PrecioBaseEntity
import org.example.appbbmges.PromotionEntity
import org.example.appbbmges.RoleEntity
import org.example.appbbmges.ScheduleEntity
import org.example.appbbmges.SnackItemEntity
import org.example.appbbmges.StudentAuthorizedAdultEntity
import org.example.appbbmges.StudentEntity
import org.example.appbbmges.StudentFamilyEntity
import org.example.appbbmges.StudentScheduleEntity
import org.example.appbbmges.TeacherEntity
import org.example.appbbmges.TeacherHourlyRateEntity
import org.example.appbbmges.TeacherPaymentEntity
import org.example.appbbmges.TeacherReportEntity
import org.example.appbbmges.TrialClassEntity
import org.example.appbbmges.UserEntity
import org.example.appbbmges.UserRoleEntity
import org.example.appbbmges.UserRolesByUser
import org.example.appbbmges.FranchiseDisciplineByFranchise
import org.example.appbbmges.FranchiseWithAddress
import org.example.appbbmges.InventoryByFranchise

class Repository(private val database: AppDatabaseBaby) {

    // ============= ROLES =============
    fun insertRole(name: String, description: String?) {
        database.expensesDbQueries.roleCreate(name, description)
    }

    fun getAllRoles(): List<RoleEntity> {
        return database.expensesDbQueries.roleSelectAll().executeAsList()
    }

    fun getRoleById(id: Long): RoleEntity? {
        return database.expensesDbQueries.roleSelectById(id).executeAsOneOrNull()
    }

    fun updateRole(id: Long, name: String, description: String?) {
        database.expensesDbQueries.roleUpdate(name, description, id)
    }

    fun deleteRole(id: Long) {
        database.expensesDbQueries.roleDelete(id)
    }

    fun getRoleCount(): Long {
        return database.expensesDbQueries.roleCount().executeAsOne()
    }

    // ============= USUARIOS =============
    fun createUser(username: String, passwordHash: String, active: Long, createdAt: Long, updatedAt: Long) {
        database.expensesDbQueries.userCreate(username, passwordHash, active, createdAt, updatedAt)
    }

    fun getUserById(id: Long): UserEntity? {
        return database.expensesDbQueries.selectUserById(id).executeAsOneOrNull()
    }

    fun getUserByUsername(username: String): UserEntity? {
        return database.expensesDbQueries.selectUserByUsername(username).executeAsOneOrNull()
    }

    fun activateUser(id: Long, updatedAt: Long) {
        database.expensesDbQueries.userActivate(updatedAt, id)
    }

    fun deactivateUser(id: Long, updatedAt: Long) {
        database.expensesDbQueries.userDeactivate(updatedAt, id)
    }

    // ============= USER ROLES =============
    fun assignUserRole(userId: Long, roleId: Long, franchiseId: Long?) {
        database.expensesDbQueries.userRoleAssign(userId, roleId, franchiseId)
    }

    fun getUserRoles(userId: Long): List<UserRolesByUser> {
        return database.expensesDbQueries.userRolesByUser(userId).executeAsList()
    }

    // ============= FRANQUICIAS =============
    fun createFranchise(
        name: String,
        email: String?,
        phone: String?,
        basePriceCents: Long,
        currency: String,
        taxName: String?,
        taxId: String?,
        zone: String?,
        isNew: Long,
        active: Long,
        addressId: Long?
    ) {
        database.expensesDbQueries.franchiseCreate(
            name, email, phone, basePriceCents, currency,
            taxName, taxId, zone, isNew, active, addressId
        )
    }

    fun getAllFranchises(): List<FranchiseEntity> {
        return database.expensesDbQueries.franchiseSelectAll().executeAsList()
    }

    fun deleteFranchise(id: Long) {
        database.expensesDbQueries.franchiseDelete(id)
    }

    // ============= NIVELES =============
    fun createLevel(name: String) {
        database.expensesDbQueries.levelCreate(name)
    }

    fun getAllLevels(): List<LevelEntity> {
        return database.expensesDbQueries.levelSelectAll().executeAsList()
    }

    // ============= DISCIPLINAS =============
    fun createDiscipline(name: String, levelId: Long) {
        database.expensesDbQueries.disciplineCreate(name, levelId)
    }

    fun getAllDisciplines(): List<DisciplineSelectAll> {
        return database.expensesDbQueries.disciplineSelectAll().executeAsList()
    }

    // ============= DISCIPLINAS POR FRANQUICIA =============
    fun createFranchiseDiscipline(franchiseId: Long, disciplineId: Long) {
        database.expensesDbQueries.franchiseDisciplineCreate(franchiseId, disciplineId)
    }

    fun getDisciplinesByFranchise(franchiseId: Long): List<FranchiseDisciplineByFranchise> {
        return database.expensesDbQueries.franchiseDisciplineByFranchise(franchiseId).executeAsList()
    }

    // ============= PROFESORES =============
    fun createTeacher(
        userId: Long?,
        firstName: String,
        lastNamePaternal: String?,
        lastNameMaternal: String?,
        phone: String?,
        email: String?,
        rfc: String?,
        startTs: Long?,
        active: Long,
        vetoed: Long
    ) {
        database.expensesDbQueries.teacherCreate(
            userId, firstName, lastNamePaternal, lastNameMaternal,
            phone, email, rfc, startTs, active, vetoed
        )
    }

    fun getTeacherById(id: Long): TeacherEntity? {
        return database.expensesDbQueries.teacherSelectById(id).executeAsOneOrNull()
    }

    // ============= PROFESORES POR FRANQUICIA =============
    fun insertFranchiseTeacher(franchiseId: Long, teacherId: Long) {
        database.expensesDbQueries.insertFranchiseTeacher(franchiseId, teacherId)
    }

    // ============= TARIFAS DE PROFESORES =============
    fun insertTeacherHourlyRate(teacherId: Long, franchiseId: Long, rateCents: Long, active: Long, createdTs: Long) {
        database.expensesDbQueries.insertTeacherHourlyRate(teacherId, franchiseId, rateCents, active, createdTs)
    }

    fun getActiveHourlyRateByTeacher(teacherId: Long, franchiseId: Long): TeacherHourlyRateEntity? {
        return database.expensesDbQueries.getActiveHourlyRateByTeacher(teacherId, franchiseId).executeAsOneOrNull()
    }

    fun deactivateTeacherRates(teacherId: Long, franchiseId: Long) {
        database.expensesDbQueries.deactivateTeacherRates(teacherId, franchiseId)
    }

    // ============= PAGOS A PROFESORES =============
    fun insertTeacherPayment(
        teacherId: Long,
        franchiseId: Long,
        totalMinutes: Long,
        rateCents: Long,
        totalPaidCents: Long,
        periodLabel: String,
        paidTs: Long,
        notes: String?
    ) {
        database.expensesDbQueries.insertTeacherPayment(
            teacherId, franchiseId, totalMinutes, rateCents,
            totalPaidCents, periodLabel, paidTs, notes
        )
    }

    fun getPaymentsByTeacher(teacherId: Long): List<TeacherPaymentEntity> {
        return database.expensesDbQueries.getPaymentsByTeacher(teacherId).executeAsList()
    }

    // ============= ADMINISTRATIVOS =============
    fun getAdministrativesByFranchise(franchiseId: Long): List<AdministrativeEntity> {
        return database.expensesDbQueries.administrativesByFranchise(franchiseId).executeAsList()
    }

    // ============= FAMILIAS =============
    fun createFamily(responsibleAdultName: String, phone: String?, email: String?) {
        database.expensesDbQueries.familyCreate(responsibleAdultName, phone, email)
    }

    fun getAllFamilies(): List<FamilyEntity> {
        return database.expensesDbQueries.familySelectAll().executeAsList()
    }

    fun deleteFamilyById(id: Long) {
        database.expensesDbQueries.familyDeleteById(id)
    }

    // ============= ESTUDIANTES Y FAMILIAS =============
    fun assignStudentToFamily(studentId: Long, familyId: Long) {
        database.expensesDbQueries.assignStudentToFamily(studentId, familyId)
    }

    fun getFamilyByStudent(studentId: Long): FamilyEntity? {
        return database.expensesDbQueries.getFamilyByStudent(studentId).executeAsOneOrNull()
    }

    // CORRECCIÓN CRÍTICA: Esta función necesita pasar studentId DOS VECES
    fun getSiblingsByStudentId(studentId: Long): List<StudentEntity> {
        return database.expensesDbQueries.getSiblingsByStudentId(studentId, studentId).executeAsList()
    }

    // ============= ADULTOS AUTORIZADOS =============
    fun getAuthorizedAdultsByStudent(studentId: Long): List<StudentAuthorizedAdultEntity> {
        return database.expensesDbQueries.authorizedAdultsByStudent(studentId).executeAsList()
    }

    // ============= AULAS =============
    fun createClassroom(franchiseId: Long, name: String) {
        database.expensesDbQueries.classroomCreate(franchiseId, name)
    }

    fun getClassroomsByFranchise(franchiseId: Long): List<ClassroomEntity> {
        return database.expensesDbQueries.classroomByFranchise(franchiseId).executeAsList()
    }

    // ============= HORARIOS =============
    fun createSchedule(
        franchiseId: Long,
        classroomId: Long,
        teacherId: Long,
        disciplineId: Long,
        dayOfWeek: Long,
        startMinutes: Long,
        endMinutes: Long
    ) {
        database.expensesDbQueries.scheduleCreate(
            franchiseId, classroomId, teacherId, disciplineId,
            dayOfWeek, startMinutes, endMinutes
        )
    }

    fun getSchedulesByFranchise(franchiseId: Long): List<ScheduleEntity> {
        return database.expensesDbQueries.schedulesByFranchise(franchiseId).executeAsList()
    }

    fun getStudentSchedules(studentId: Long): List<ScheduleEntity> {
        return database.expensesDbQueries.studentSchedules(studentId).executeAsList()
    }

    // ============= ASISTENCIAS =============
    fun insertClassAttendance(scheduleId: Long, teacherId: Long, dateTs: Long, durationMinutes: Long, validated: Long) {
        database.expensesDbQueries.insertClassAttendance(scheduleId, teacherId, dateTs, durationMinutes, validated)
    }

    fun getAttendanceByTeacherAndPeriod(teacherId: Long, fromTs: Long, toTs: Long): List<ClassAttendanceEntity> {
        return database.expensesDbQueries.getAttendanceByTeacherAndPeriod(teacherId, fromTs, toTs).executeAsList()
    }

    // ============= INVENTARIO BOUTIQUE =============
    fun getInventoryByFranchise(franchiseId: Long): List<InventoryByFranchise> {
        return database.expensesDbQueries.inventoryByFranchise(franchiseId).executeAsList()
    }

    // ============= PAGOS DE ESTUDIANTES =============
    fun getPaymentsByStudentId(studentId: Long): List<PaymentEntity> {
        return database.expensesDbQueries.getPaymentsByStudentId(studentId).executeAsList()
    }

    // ============= PAGOS DE EVENTOS =============
    fun getEventPaymentsByStudent(studentId: Long): List<EventPaymentEntity> {
        return database.expensesDbQueries.eventPaymentsByStudent(studentId).executeAsList()
    }

    // ============= REPORTES DE PROFESORES =============
    fun getTeacherReportsByTeacher(teacherId: Long): List<TeacherReportEntity> {
        return database.expensesDbQueries.teacherReportsByTeacher(teacherId).executeAsList()
    }

    // ============= CLASES DE PRUEBA =============
    fun getTrialClassesByFranchise(franchiseId: Long): List<TrialClassEntity> {
        return database.expensesDbQueries.trialClassesByFranchise(franchiseId).executeAsList()
    }

    fun getAllPreciosBase(): List<PrecioBaseEntity> {
        return database.expensesDbQueries.selectAllPrecioBase().executeAsList()
    }

    fun deletePrecioBase(id: Long) {
        database.expensesDbQueries.deletePrecioBase(id)
    }

    fun getAllInscriptions(): List<InscriptionEntity> {
        return database.expensesDbQueries.selectAllInscription().executeAsList()
    }

    fun deleteInscription(id: Long) {
        database.expensesDbQueries.deleteInscription(id)
    }

    fun getAllMemberships(): List<MembershipEntity> {
        return database.expensesDbQueries.selectAllMembership().executeAsList()
    }

    fun deleteMembership(id: Long) {
        database.expensesDbQueries.deleteMembership(id)
    }

    fun getAllFranchisesWithAddress(): List<FranchiseWithAddress> {
        return database.expensesDbQueries.franchiseWithAddress().executeAsList()
    }

    // ============= PROMOCIONES =============
    fun insertPromotion(
        name: String,
        startTs: Long,
        endTs: Long,
        discountType: String,
        percentDiscount: Long?,
        amountCents: Long?,
        franchiseId: Long?,
        applicableToNew: Long,
        applicableToActive: Long
    ) {
        database.expensesDbQueries.promotionCreate(
            name, startTs, endTs, discountType,
            percentDiscount, amountCents, franchiseId,
            applicableToNew, applicableToActive
        )
    }

    // ============= ITEMS BOUTIQUE =============
    fun getBoutiqueItemByCode(code: String): BoutiqueItemEntity? {
        return database.expensesDbQueries.getBoutiqueItemByCode(code).executeAsOneOrNull()
    }

    fun insertBoutiqueItem(
        description: String,
        code: String,
        line: String?,
        franchisePrice: Double?,
        suggestedPrice: Double?,
        country: String?
    ) {
        val franchisePriceCents = franchisePrice?.times(100)?.toLong() ?: 0L
        val suggestedPriceCents = suggestedPrice?.times(100)?.toLong() ?: 0L

        database.expensesDbQueries.boutiqueItemCreate(
            description, code, line, franchisePriceCents, suggestedPriceCents, country
        )
    }

    fun insertDisciplinesWithLevelsBatch(baseName: String, levelIds: List<Long>): List<Pair<String, Long>> {
        val failedInsertions = mutableListOf<Pair<String, Long>>()

        levelIds.forEach { levelId ->
            try {
                val level = database.expensesDbQueries.levelSelectById(levelId).executeAsOneOrNull()
                if (level != null) {
                    val fullName = "$baseName ${level.name}".trim()
                    database.expensesDbQueries.disciplineCreate(fullName, levelId)
                }
            } catch (e: Exception) {
                val level = database.expensesDbQueries.levelSelectById(levelId).executeAsOneOrNull()
                if (level != null) {
                    val fullName = "$baseName ${level.name}".trim()
                    failedInsertions.add(Pair(fullName, levelId))
                }
            }
        }

        return failedInsertions
    }


    // ============= DASHBOARD HELPERS =============

    fun getStudentCount(): Long {
        return database.expensesDbQueries.studentCount().executeAsOne()
    }

    fun getTeacherCount(): Long {
        return database.expensesDbQueries.teacherCount().executeAsOne()
    }

    fun getActiveBranchesCount(): Long {
        return database.expensesDbQueries.activeFranchisesCount().executeAsOne()
    }

    fun getAllStudents(): List<StudentEntity> {
        return database.expensesDbQueries.selectAllStudents().executeAsList()
    }

    fun getStudentsByGender(): Pair<Long, Long> {
        val students = database.expensesDbQueries
            .selectAllStudents()
            .executeAsList()

        val maleCount = students.count { it.curp?.getOrNull(10) == 'H' }.toLong()
        val femaleCount = students.count { it.curp?.getOrNull(10) == 'M' }.toLong()

        return Pair(maleCount, femaleCount)
    }



    fun initializeData() {
        val existingRoles = getRoleCount()
        if (existingRoles == 0L) {
            val currentTime = Clock.System.now().toEpochMilliseconds()

            // Crear usuario
            createUser(
                username = "Adminpresi12",
                passwordHash = "Adminpresi12",
                active = 1L,
                createdAt = currentTime,
                updatedAt = currentTime
            )

            assignUserRole(
                userId = 1L,
                roleId = 1L,
                franchiseId = 1L
            )
        }
    }
}