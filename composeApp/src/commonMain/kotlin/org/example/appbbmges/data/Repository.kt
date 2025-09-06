package org.example.appbbmges.data

import org.example.appbbmges.AddressEntity
import org.example.appbbmges.AppDatabaseBaby
import org.example.appbbmges.ClassroomEntity
import org.example.appbbmges.DisciplineSelectAll
import org.example.appbbmges.EventPaymentEntity
import org.example.appbbmges.FranchiseEntity
import org.example.appbbmges.InscriptionEntity
import org.example.appbbmges.InventoryByFranchise
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
import org.example.appbbmges.TeacherEntity
import org.example.appbbmges.TeacherReportEntity
import org.example.appbbmges.TrialClassEntity
import org.example.appbbmges.UserEntity
import org.example.appbbmges.UserRolesByUser
import org.example.appbbmges.FranchiseDisciplineByFranchise
import org.example.appbbmges.AdministrativeEntity
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class Repository(private val database: AppDatabaseBaby) {

    // --- Inicialización ---
    @OptIn(ExperimentalTime::class)
    fun initializeData() {
        // Roles básicos
        if (getAllRoles().isEmpty()) {
            insertRole("ADMIN", "Super administrador del sistema")
            insertRole("FRANCHISEE", "Franquiciatario")
            insertRole("TEACHER", "Profesor")
            insertRole("STUDENT", "Alumno / Padre de familia")
        }

        // Usuario admin inicial
        if (getUserByUsername("admin") == null) {
            val currentTime = Clock.System.now().toEpochMilliseconds()
            createUser(
                username = "admin",
                passwordHash = "admin123",
                active = 1,
                createdAt = currentTime,
                updatedAt = currentTime
            )
            val adminUser = getUserByUsername("admin")
            val adminRole = getAllRoles().find { it.name == "ADMIN" }
            if (adminUser != null && adminRole != null) {
                assignUserRole(adminUser.id, adminRole.id, null)
            }
        }
    }

    // --- Roles ---
    fun insertRole(name: String, description: String?) {
        require(name.isNotBlank()) { "El nombre del rol no puede estar vacío" }
        database.expensesDbQueries.roleCreate(name, description)
    }

    fun getAllRoles(): List<RoleEntity> =
        database.expensesDbQueries.roleSelectAll().executeAsList()

    fun deleteRole(id: Long) {
        database.expensesDbQueries.roleDelete(id)
    }

    // --- Usuarios ---
    fun createUser(username: String, passwordHash: String, active: Long, createdAt: Long, updatedAt: Long) {
        require(username.isNotBlank()) { "El usuario no puede estar vacío" }
        require(passwordHash.isNotBlank()) { "La contraseña no puede estar vacía" }
        database.expensesDbQueries.userCreate(username, passwordHash, active, createdAt, updatedAt)
    }

    fun getUserById(id: Long): UserEntity? =
        database.expensesDbQueries.selectUserById(id).executeAsOneOrNull()

    fun getUserByUsername(username: String): UserEntity? =
        database.expensesDbQueries.selectUserByUsername(username).executeAsOneOrNull()

    fun activateUser(id: Long, updatedAt: Long) {
        database.expensesDbQueries.userActivate(updatedAt, id)
    }

    fun deactivateUser(id: Long, updatedAt: Long) {
        database.expensesDbQueries.userDeactivate(updatedAt, id)
    }

    // --- Roles por usuario ---
    fun assignUserRole(userId: Long, roleId: Long, franchiseId: Long?) {
        database.expensesDbQueries.userRoleAssign(userId, roleId, franchiseId)
    }

    fun getUserRoles(userId: Long): List<UserRolesByUser> =
        database.expensesDbQueries.userRolesByUser(userId).executeAsList()

    // --- Franquicias ---
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
        require(basePriceCents >= 0) { "El precio base no puede ser negativo" }
        database.expensesDbQueries.franchiseCreate(
            name, email, phone, basePriceCents, currency,
            taxName, taxId, zone, isNew, active, addressId
        )
    }

    fun getAllFranchises(): List<FranchiseEntity> =
        database.expensesDbQueries.franchiseSelectAll().executeAsList()

    fun deleteFranchise(id: Long) {
        database.expensesDbQueries.franchiseDelete(id)
    }

    // --- Niveles y disciplinas ---
    fun createLevel(name: String) {
        database.expensesDbQueries.levelCreate(name)
    }

    fun getAllLevels(): List<LevelEntity> =
        database.expensesDbQueries.levelSelectAll().executeAsList()

    fun createDiscipline(name: String, levelId: Long) {
        database.expensesDbQueries.disciplineCreate(name, levelId)
    }

    fun getAllDisciplines(): List<DisciplineSelectAll> =
        database.expensesDbQueries.disciplineSelectAll().executeAsList()

    fun addDisciplineToFranchise(franchiseId: Long, disciplineId: Long) {
        database.expensesDbQueries.franchiseDisciplineCreate(franchiseId, disciplineId)
    }

    fun getFranchiseDisciplines(franchiseId: Long): List<FranchiseDisciplineByFranchise> =
        database.expensesDbQueries.franchiseDisciplineByFranchise(franchiseId).executeAsList()

    // --- Profesores ---
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

    fun getTeacherById(id: Long): TeacherEntity? =
        database.expensesDbQueries.teacherSelectById(id).executeAsOneOrNull()

    fun assignTeacherToFranchise(franchiseId: Long, teacherId: Long) {
        database.expensesDbQueries.insertFranchiseTeacher(franchiseId, teacherId)
    }

    // --- Administrativos ---
    fun getAdministrativesByFranchise(franchiseId: Long): List<AdministrativeEntity> =
        database.expensesDbQueries.administrativesByFranchise(franchiseId).executeAsList()

    // --- Alumnos ---
    fun getAuthorizedAdultsByStudent(studentId: Long): List<StudentAuthorizedAdultEntity> =
        database.expensesDbQueries.authorizedAdultsByStudent(studentId).executeAsList()

    // --- Aulas y horarios ---
    fun createClassroom(franchiseId: Long, name: String) {
        database.expensesDbQueries.classroomCreate(franchiseId, name)
    }

    fun getClassroomsByFranchise(franchiseId: Long): List<ClassroomEntity> =
        database.expensesDbQueries.classroomByFranchise(franchiseId).executeAsList()

    fun createSchedule(
        franchiseId: Long,
        classroomId: Long,
        teacherId: Long,
        disciplineId: Long,
        dayOfWeek: Long,
        startMinutes: Long,
        endMinutes: Long
    ) {
        require(startMinutes < endMinutes) { "La hora de inicio no puede ser mayor o igual a la de fin" }
        database.expensesDbQueries.scheduleCreate(
            franchiseId, classroomId, teacherId, disciplineId,
            dayOfWeek, startMinutes, endMinutes
        )
    }

    fun getSchedulesByFranchise(franchiseId: Long): List<ScheduleEntity> =
        database.expensesDbQueries.schedulesByFranchise(franchiseId).executeAsList()

    fun getStudentSchedules(studentId: Long): List<ScheduleEntity> =
        database.expensesDbQueries.studentSchedules(studentId).executeAsList()

    // --- Inventario ---
    fun getInventoryByFranchise(franchiseId: Long): List<InventoryByFranchise> =
        database.expensesDbQueries.inventoryByFranchise(franchiseId).executeAsList()

    // --- Pagos ---
    fun getPaymentsByStudent(studentId: Long): List<PaymentEntity> =
        database.expensesDbQueries.getPaymentsByStudentId(studentId).executeAsList()

    // --- Eventos ---
    fun getEventPaymentsByStudent(studentId: Long): List<EventPaymentEntity> =
        database.expensesDbQueries.eventPaymentsByStudent(studentId).executeAsList()

    // --- Reportes ---
    fun getTeacherReportsByTeacher(teacherId: Long): List<TeacherReportEntity> =
        database.expensesDbQueries.teacherReportsByTeacher(teacherId).executeAsList()

    // --- Clases muestra ---
    fun getTrialClassesByFranchise(franchiseId: Long): List<TrialClassEntity> =
        database.expensesDbQueries.trialClassesByFranchise(franchiseId).executeAsList()
}