package org.example.appbbmges.data

import org.example.appbbmges.AdministrativeEntity
import org.example.appbbmges.AppDatabaseBaby
import org.example.appbbmges.BoutiqueItemEntity
import org.example.appbbmges.ClassroomEntity
import org.example.appbbmges.DisciplineSelectAll
import org.example.appbbmges.DisciplineSelectByBaseName
import org.example.appbbmges.DisciplineSelectById
import org.example.appbbmges.EventEntity
import org.example.appbbmges.FranchiseBoutiqueInventoryEntity
import org.example.appbbmges.FranchiseDisciplineEntity
import org.example.appbbmges.FranchiseEntity
import org.example.appbbmges.FranchiseTeacherEntity
import org.example.appbbmges.FranchiseeEntity
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
import org.example.appbbmges.StudentScheduleEntity
import org.example.appbbmges.TeacherDisciplineEntity
import org.example.appbbmges.TeacherEntity
import org.example.appbbmges.TeacherReportEntity
import org.example.appbbmges.TrialClassEntity
import org.example.appbbmges.UserEntity

class Repository(private val database: AppDatabaseBaby) {

    // --- RoleEntity ---
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

    // --- UserEntity ---
    fun insertUser(username: String, password: String, roleId: Long, franchiseId: Long, active: Long = 1) {
        database.expensesDbQueries.userCreate(username, password, roleId, franchiseId, active)
    }

    fun getAllUsers(): List<UserEntity> {
        return database.expensesDbQueries.selectAllUsers().executeAsList()
    }

    fun getUserByUsername(username: String): UserEntity? {
        return database.expensesDbQueries.selectUserByUsername(username).executeAsOneOrNull()
    }

    fun getUserById(id: Long): UserEntity? {
        return database.expensesDbQueries.selectUserById(id).executeAsOneOrNull()
    }

    fun updateUser(id: Long, newUsername: String, newPassword: String) {
        database.expensesDbQueries.updateUser(newUsername, newPassword, id)
    }

    fun deleteUser(id: Long) {
        database.expensesDbQueries.deleteUser(id)
    }

    fun getUserCount(): Long {
        return database.expensesDbQueries.countUsers().executeAsOne()
    }

    // --- FranchiseEntity ---
    fun insertFranchise(
        name: String, email: String?, phone: String?, basePrice: Double?, currency: String?,
        addressStreet: String?, addressNumber: String?, addressNeighborhood: String?, addressZip: String?,
        addressCity: String?, addressCountry: String?, taxName: String?, taxId: String?, zone: String?,
        isNew: Long = 0, active: Long = 1
    ) {
        database.expensesDbQueries.franchiseCreate(
            name, email, phone, basePrice, currency, addressStreet, addressNumber, addressNeighborhood,
            addressZip, addressCity, addressCountry, taxName, taxId, zone, isNew, active
        )
    }

    fun getAllFranchises(): List<FranchiseEntity> {
        return database.expensesDbQueries.franchiseSelectAll().executeAsList()
    }

    fun getFranchiseById(id: Long): FranchiseEntity? {
        return database.expensesDbQueries.franchiseSelectById(id).executeAsOneOrNull()
    }

    fun updateFranchise(
        id: Long, name: String, email: String?, phone: String?, basePrice: Double?, currency: String?,
        addressStreet: String?, addressNumber: String?, addressNeighborhood: String?, addressZip: String?,
        addressCity: String?, addressCountry: String?, taxName: String?, taxId: String?, zone: String?,
        isNew: Long, active: Long
    ) {
        database.expensesDbQueries.franchiseUpdate(
            name, email, phone, basePrice, currency, addressStreet, addressNumber, addressNeighborhood,
            addressZip, addressCity, addressCountry, taxName, taxId, zone, isNew, active, id
        )
    }

    fun deleteFranchise(id: Long) {
        database.expensesDbQueries.franchiseDelete(id)
    }

    fun getFranchiseCount(): Long {
        return database.expensesDbQueries.franchiseCount().executeAsOne()
    }

    // --- LevelEntity ---
    fun insertLevel(name: String) {
        database.expensesDbQueries.levelCreate(name)
    }

    fun getAllLevels(): List<LevelEntity> {
        return database.expensesDbQueries.levelSelectAll().executeAsList()
    }

    fun getLevelById(id: Long): LevelEntity? {
        return database.expensesDbQueries.levelSelectById(id).executeAsOneOrNull()
    }

    fun updateLevel(id: Long, name: String) {
        database.expensesDbQueries.levelUpdate(name, id)
    }

    fun deleteLevel(id: Long) {
        database.expensesDbQueries.levelDelete(id)
    }

    fun getLevelCount(): Long {
        return database.expensesDbQueries.levelCount().executeAsOne()
    }

    // --- DisciplineEntity ---
    fun insertDiscipline(name: String, levelId: Long) {
        database.expensesDbQueries.disciplineCreate(name, levelId)
    }

    fun insertDisciplinesWithLevelsBatch(baseName: String, levelIds: List<Long>): List<Pair<String, Long>> {
        val failedInsertions = mutableListOf<Pair<String, Long>>()

        database.expensesDbQueries.transaction {
            levelIds.forEach { levelId ->
                val level = getLevelById(levelId)
                if (level == null) {
                    return@forEach
                }

                val disciplineName = "$baseName ${level.name}".trim()
                try {
                    database.expensesDbQueries.disciplineCreate(disciplineName, levelId)
                } catch (e: Exception) {
                    if (e.message?.contains("UNIQUE constraint failed", ignoreCase = true) == true) {
                        failedInsertions.add(Pair(disciplineName, levelId))
                    } else {
                        throw e
                    }
                }
            }
        }
        return failedInsertions
    }

    fun getAllDisciplines(): List<DisciplineSelectAll> {
        return database.expensesDbQueries.disciplineSelectAll().executeAsList()
    }

    fun getDisciplineById(id: Long): DisciplineSelectById? {
        return database.expensesDbQueries.disciplineSelectById(id).executeAsOneOrNull()
    }

    fun getDisciplinesByBaseName(baseName: String): List<DisciplineSelectByBaseName> {
        return database.expensesDbQueries.disciplineSelectByBaseName(baseName).executeAsList()
    }

    fun updateDiscipline(id: Long, name: String, levelId: Long) {
        database.expensesDbQueries.disciplineUpdate(name, levelId, id)
    }

    fun deleteDiscipline(id: Long) {
        database.expensesDbQueries.disciplineDelete(id)
    }

    fun getDisciplineCount(): Long {
        return database.expensesDbQueries.disciplineCount().executeAsOne()
    }

    // --- FranchiseDisciplineEntity ---
    fun insertFranchiseDiscipline(franchiseId: Long, disciplineId: Long) {
        database.expensesDbQueries.franchiseDisciplineCreate(franchiseId, disciplineId)
    }

    fun getAllFranchiseDisciplines(): List<FranchiseDisciplineEntity> {
        return database.expensesDbQueries.franchiseDisciplineSelectAll().executeAsList()
    }

    fun getFranchiseDisciplinesByFranchiseId(franchiseId: Long): List<FranchiseDisciplineEntity> {
        return database.expensesDbQueries.franchiseDisciplineSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateFranchiseDiscipline(franchiseId: Long, oldDisciplineId: Long, newDisciplineId: Long) {
        database.expensesDbQueries.franchiseDisciplineUpdate(newDisciplineId, franchiseId, oldDisciplineId)
    }

    fun deleteFranchiseDiscipline(franchiseId: Long, disciplineId: Long) {
        database.expensesDbQueries.franchiseDisciplineDelete(franchiseId, disciplineId)
    }

    fun getFranchiseDisciplineCount(): Long {
        return database.expensesDbQueries.franchiseDisciplineCount().executeAsOne()
    }

    // --- ClassroomEntity ---
    fun insertClassroom(franchiseId: Long, name: String) {
        database.expensesDbQueries.classroomCreate(franchiseId, name)
    }

    fun getAllClassrooms(): List<ClassroomEntity> {
        return database.expensesDbQueries.classroomSelectAll().executeAsList()
    }

    fun getClassroomById(id: Long): ClassroomEntity? {
        return database.expensesDbQueries.classroomSelectById(id).executeAsOneOrNull()
    }

    fun getClassroomsByFranchiseId(franchiseId: Long): List<ClassroomEntity> {
        return database.expensesDbQueries.classroomSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateClassroom(id: Long, franchiseId: Long, name: String) {
        database.expensesDbQueries.classroomUpdate(franchiseId, name, id)
    }

    fun deleteClassroom(id: Long) {
        database.expensesDbQueries.classroomDelete(id)
    }

    fun getClassroomCount(): Long {
        return database.expensesDbQueries.classroomCount().executeAsOne()
    }

    // --- TeacherEntity ---
    fun insertTeacher(
        firstName: String, lastNamePaternal: String?, lastNameMaternal: String?, gender: String?,
        birthDate: String?, nationality: String?, taxId: String?, phone: String?, email: String?,
        addressStreet: String?, addressZip: String?, emergencyContactName: String?,
        emergencyContactPhone: String?, salaryPerHour: Double?, startDate: String?,
        active: Long = 1, vetoed: Long = 0
    ) {
        database.expensesDbQueries.teacherCreate(
            firstName, lastNamePaternal, lastNameMaternal, gender, birthDate, nationality, taxId,
            phone, email, addressStreet, addressZip, emergencyContactName, emergencyContactPhone,
            salaryPerHour, startDate, active, vetoed
        )
    }

    fun getAllTeachers(): List<TeacherEntity> {
        return database.expensesDbQueries.teacherSelectAll().executeAsList()
    }

    fun getTeacherById(id: Long): TeacherEntity? {
        return database.expensesDbQueries.teacherSelectById(id).executeAsOneOrNull()
    }

    fun updateTeacher(
        id: Long, firstName: String, lastNamePaternal: String?, lastNameMaternal: String?, gender: String?,
        birthDate: String?, nationality: String?, taxId: String?, phone: String?, email: String?,
        addressStreet: String?, addressZip: String?, emergencyContactName: String?,
        emergencyContactPhone: String?, salaryPerHour: Double?, startDate: String?,
        active: Long, vetoed: Long
    ) {
        database.expensesDbQueries.teacherUpdate(
            firstName, lastNamePaternal, lastNameMaternal, gender, birthDate, nationality, taxId,
            phone, email, addressStreet, addressZip, emergencyContactName, emergencyContactPhone,
            salaryPerHour, startDate, active, vetoed, id
        )
    }

    fun deleteTeacher(id: Long) {
        database.expensesDbQueries.teacherDelete(id)
    }

    fun getTeacherCount(): Long {
        return database.expensesDbQueries.teacherCount().executeAsOne()
    }

    // --- FranchiseeEntity ---
    fun insertFranchisee(
        franchiseId: Long,
        firstName: String,
        lastNamePaternal: String?,
        lastNameMaternal: String?,
        gender: String?,
        birthDate: String?,
        nationality: String?,
        taxId: String?,
        phone: String?,
        email: String?,
        addressStreet: String?,
        addressZip: String?,
        emergencyContactName: String?,
        emergencyContactPhone: String?,
        startDate: String?,
        active: Long = 1
    ) {
        database.expensesDbQueries.franchiseeCreate(
            franchiseId, firstName, lastNamePaternal, lastNameMaternal, gender, birthDate,
            nationality, taxId, phone, email, addressStreet, addressZip,
            emergencyContactName, emergencyContactPhone, startDate, active
        )
    }

    fun getAllFranchisees(): List<FranchiseeEntity> {
        return database.expensesDbQueries.franchiseeSelectAll().executeAsList()
    }

    fun getFranchiseeById(id: Long): FranchiseeEntity? {
        return database.expensesDbQueries.franchiseeSelectById(id).executeAsOneOrNull()
    }

    fun getFranchiseesByFranchiseId(franchiseId: Long): List<FranchiseeEntity> {
        return database.expensesDbQueries.franchiseeSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateFranchisee(
        id: Long,
        franchiseId: Long,
        firstName: String,
        lastNamePaternal: String?,
        lastNameMaternal: String?,
        gender: String?,
        birthDate: String?,
        nationality: String?,
        taxId: String?,
        phone: String?,
        email: String?,
        addressStreet: String?,
        addressZip: String?,
        emergencyContactName: String?,
        emergencyContactPhone: String?,
        startDate: String?,
        active: Long
    ) {
        database.expensesDbQueries.franchiseeUpdate(
            franchiseId, firstName, lastNamePaternal, lastNameMaternal, gender, birthDate,
            nationality, taxId, phone, email, addressStreet, addressZip,
            emergencyContactName, emergencyContactPhone, startDate, active, id
        )
    }

    fun deleteFranchisee(id: Long) {
        database.expensesDbQueries.franchiseeDelete(id)
    }

    fun getFranchiseeCount(): Long {
        return database.expensesDbQueries.franchiseeCount().executeAsOne()
    }

    // --- FranchiseTeacherEntity ---
    fun insertFranchiseTeacher(franchiseId: Long, teacherId: Long) {
        database.expensesDbQueries.franchiseTeacherCreate(franchiseId, teacherId)
    }

    fun getAllFranchiseTeachers(): List<FranchiseTeacherEntity> {
        return database.expensesDbQueries.franchiseTeacherSelectAll().executeAsList()
    }

    fun getFranchiseTeachersByFranchiseId(franchiseId: Long): List<FranchiseTeacherEntity> {
        return database.expensesDbQueries.franchiseTeacherSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateFranchiseTeacher(franchiseId: Long, oldTeacherId: Long, newTeacherId: Long) {
        database.expensesDbQueries.franchiseTeacherUpdate(newTeacherId, franchiseId, oldTeacherId)
    }

    fun deleteFranchiseTeacher(franchiseId: Long, teacherId: Long) {
        database.expensesDbQueries.franchiseTeacherDelete(franchiseId, teacherId)
    }

    fun getFranchiseTeacherCount(): Long {
        return database.expensesDbQueries.franchiseTeacherCount().executeAsOne()
    }

    // --- TeacherDisciplineEntity ---
    fun insertTeacherDiscipline(teacherId: Long, disciplineId: Long) {
        database.expensesDbQueries.teacherDisciplineCreate(teacherId, disciplineId)
    }

    fun getAllTeacherDisciplines(): List<TeacherDisciplineEntity> {
        return database.expensesDbQueries.teacherDisciplineSelectAll().executeAsList()
    }

    fun getTeacherDisciplinesByTeacherId(teacherId: Long): List<TeacherDisciplineEntity> {
        return database.expensesDbQueries.teacherDisciplineSelectByTeacherId(teacherId).executeAsList()
    }

    fun updateTeacherDiscipline(teacherId: Long, oldDisciplineId: Long, newDisciplineId: Long) {
        database.expensesDbQueries.teacherDisciplineUpdate(newDisciplineId, teacherId, oldDisciplineId)
    }

    fun deleteTeacherDiscipline(teacherId: Long, disciplineId: Long) {
        database.expensesDbQueries.teacherDisciplineDelete(teacherId, disciplineId)
    }

    fun getTeacherDisciplineCount(): Long {
        return database.expensesDbQueries.teacherDisciplineCount().executeAsOne()
    }

    // --- StudentEntity ---
    fun insertStudent(
        franchiseId: Long, firstName: String, lastNamePaternal: String?, lastNameMaternal: String?,
        gender: String?, birthDate: String?, nationality: String?, curp: String?, phone: String?,
        email: String?, addressStreet: String?, addressZip: String?, parentFatherFirstName: String?,
        parentFatherLastNamePaternal: String?, parentFatherLastNameMaternal: String?,
        parentMotherFirstName: String?, parentMotherLastNamePaternal: String?,
        parentMotherLastNameMaternal: String?, bloodType: String?, chronicDisease: String?,
        active: Long = 1
    ) {
        database.expensesDbQueries.studentCreate(
            franchiseId, firstName, lastNamePaternal, lastNameMaternal, gender, birthDate, nationality,
            curp, phone, email, addressStreet, addressZip, parentFatherFirstName, parentFatherLastNamePaternal,
            parentFatherLastNameMaternal, parentMotherFirstName, parentMotherLastNamePaternal,
            parentMotherLastNameMaternal, bloodType, chronicDisease, active
        )
    }

    fun getAllStudents(): List<StudentEntity> {
        return database.expensesDbQueries.studentSelectAll().executeAsList()
    }

    fun getStudentById(id: Long): StudentEntity? {
        return database.expensesDbQueries.studentSelectById(id).executeAsOneOrNull()
    }

    fun getStudentsByFranchiseId(franchiseId: Long): List<StudentEntity> {
        return database.expensesDbQueries.studentSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateStudent(
        id: Long, franchiseId: Long, firstName: String, lastNamePaternal: String?, lastNameMaternal: String?,
        gender: String?, birthDate: String?, nationality: String?, curp: String?, phone: String?,
        email: String?, addressStreet: String?, addressZip: String?, parentFatherFirstName: String?,
        parentFatherLastNamePaternal: String?, parentFatherLastNameMaternal: String?,
        parentMotherFirstName: String?, parentMotherLastNamePaternal: String?,
        parentMotherLastNameMaternal: String?, bloodType: String?, chronicDisease: String?,
        active: Long
    ) {
        database.expensesDbQueries.studentUpdate(
            franchiseId, firstName, lastNamePaternal, lastNameMaternal, gender, birthDate, nationality,
            curp, phone, email, addressStreet, addressZip, parentFatherFirstName, parentFatherLastNamePaternal,
            parentFatherLastNameMaternal, parentMotherFirstName, parentMotherLastNamePaternal,
            parentMotherLastNameMaternal, bloodType, chronicDisease, active, id
        )
    }

    fun deleteStudent(id: Long) {
        database.expensesDbQueries.studentDelete(id)
    }

    fun getStudentCount(): Long {
        return database.expensesDbQueries.studentCount().executeAsOne()
    }

    // --- StudentAuthorizedAdultEntity ---
    fun insertStudentAuthorizedAdult(studentId: Long, firstName: String, lastNamePaternal: String?, lastNameMaternal: String?) {
        database.expensesDbQueries.studentAuthorizedAdultCreate(studentId, firstName, lastNamePaternal, lastNameMaternal)
    }

    fun getAllStudentAuthorizedAdults(): List<StudentAuthorizedAdultEntity> {
        return database.expensesDbQueries.studentAuthorizedAdultSelectAll().executeAsList()
    }

    fun getStudentAuthorizedAdultById(id: Long): StudentAuthorizedAdultEntity? {
        return database.expensesDbQueries.studentAuthorizedAdultSelectById(id).executeAsOneOrNull()
    }

    fun getStudentAuthorizedAdultsByStudentId(studentId: Long): List<StudentAuthorizedAdultEntity> {
        return database.expensesDbQueries.studentAuthorizedAdultSelectByStudentId(studentId).executeAsList()
    }

    fun updateStudentAuthorizedAdult(id: Long, studentId: Long, firstName: String, lastNamePaternal: String?, lastNameMaternal: String?) {
        database.expensesDbQueries.studentAuthorizedAdultUpdate(studentId, firstName, lastNamePaternal, lastNameMaternal, id)
    }

    fun deleteStudentAuthorizedAdult(id: Long) {
        database.expensesDbQueries.studentAuthorizedAdultDelete(id)
    }

    fun getStudentAuthorizedAdultCount(): Long {
        return database.expensesDbQueries.studentAuthorizedAdultCount().executeAsOne()
    }

    // --- ScheduleEntity ---
    fun insertSchedule(franchiseId: Long, classroomId: Long, teacherId: Long, disciplineId: Long, dayOfWeek: Long, startTime: String, endTime: String) {
        database.expensesDbQueries.scheduleCreate(franchiseId, classroomId, teacherId, disciplineId, dayOfWeek, startTime, endTime)
    }

    fun getAllSchedules(): List<ScheduleEntity> {
        return database.expensesDbQueries.scheduleSelectAll().executeAsList()
    }

    fun getScheduleById(id: Long): ScheduleEntity? {
        return database.expensesDbQueries.scheduleSelectById(id).executeAsOneOrNull()
    }

    fun getSchedulesByFranchiseId(franchiseId: Long): List<ScheduleEntity> {
        return database.expensesDbQueries.scheduleSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateSchedule(id: Long, franchiseId: Long, classroomId: Long, teacherId: Long, disciplineId: Long, dayOfWeek: Long, startTime: String, endTime: String) {
        database.expensesDbQueries.scheduleUpdate(franchiseId, classroomId, teacherId, disciplineId, dayOfWeek, startTime, endTime, id)
    }

    fun deleteSchedule(id: Long) {
        database.expensesDbQueries.scheduleDelete(id)
    }

    fun getScheduleCount(): Long {
        return database.expensesDbQueries.scheduleCount().executeAsOne()
    }

    // --- StudentScheduleEntity ---
    fun insertStudentSchedule(studentId: Long, scheduleId: Long) {
        database.expensesDbQueries.studentScheduleCreate(studentId, scheduleId)
    }

    fun getAllStudentSchedules(): List<StudentScheduleEntity> {
        return database.expensesDbQueries.studentScheduleSelectAll().executeAsList()
    }

    fun getStudentSchedulesByStudentId(studentId: Long): List<StudentScheduleEntity> {
        return database.expensesDbQueries.studentScheduleSelectByStudentId(studentId).executeAsList()
    }

    fun updateStudentSchedule(studentId: Long, oldScheduleId: Long, newScheduleId: Long) {
        database.expensesDbQueries.studentScheduleUpdate(newScheduleId, studentId, oldScheduleId)
    }

    fun deleteStudentSchedule(studentId: Long, scheduleId: Long) {
        database.expensesDbQueries.studentScheduleDelete(studentId, scheduleId)
    }

    fun getStudentScheduleCount(): Long {
        return database.expensesDbQueries.studentScheduleCount().executeAsOne()
    }

    // --- BoutiqueItemEntity ---
    fun insertBoutiqueItem(description: String, code: String, line: String?, franchisePrice: Double?, suggestedPrice: Double?, country: String?) {
        database.expensesDbQueries.boutiqueItemCreate(description, code, line, franchisePrice, suggestedPrice, country)
    }

    fun getAllBoutiqueItems(): List<BoutiqueItemEntity> {
        return database.expensesDbQueries.boutiqueItemSelectAll().executeAsList()
    }

    fun getBoutiqueItemById(id: Long): BoutiqueItemEntity? {
        return database.expensesDbQueries.boutiqueItemSelectById(id).executeAsOneOrNull()
    }

    fun getBoutiqueItemByCode(code: String): BoutiqueItemEntity? {
        return database.expensesDbQueries.boutiqueItemSelectByCode(code).executeAsOneOrNull()
    }

    fun updateBoutiqueItem(id: Long, description: String, code: String, line: String?, franchisePrice: Double?, suggestedPrice: Double?, country: String?) {
        database.expensesDbQueries.boutiqueItemUpdate(description, code, line, franchisePrice, suggestedPrice, country, id)
    }

    fun deleteBoutiqueItem(id: Long) {
        database.expensesDbQueries.boutiqueItemDelete(id)
    }

    fun getBoutiqueItemCount(): Long {
        return database.expensesDbQueries.boutiqueItemCount().executeAsOne()
    }

    // --- FranchiseBoutiqueInventoryEntity ---
    fun insertFranchiseBoutiqueInventory(franchiseId: Long, boutiqueItemId: Long, stock: Long, salePrice: Double?) {
        database.expensesDbQueries.franchiseBoutiqueInventoryCreate(franchiseId, boutiqueItemId, stock, salePrice)
    }

    fun getAllFranchiseBoutiqueInventories(): List<FranchiseBoutiqueInventoryEntity> {
        return database.expensesDbQueries.franchiseBoutiqueInventorySelectAll().executeAsList()
    }

    fun getFranchiseBoutiqueInventoriesByFranchiseId(franchiseId: Long): List<FranchiseBoutiqueInventoryEntity> {
        return database.expensesDbQueries.franchiseBoutiqueInventorySelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateFranchiseBoutiqueInventory(franchiseId: Long, boutiqueItemId: Long, stock: Long, salePrice: Double?) {
        database.expensesDbQueries.franchiseBoutiqueInventoryUpdate(stock, salePrice, franchiseId, boutiqueItemId)
    }

    fun deleteFranchiseBoutiqueInventory(franchiseId: Long, boutiqueItemId: Long) {
        database.expensesDbQueries.franchiseBoutiqueInventoryDelete(franchiseId, boutiqueItemId)
    }

    fun getFranchiseBoutiqueInventoryCount(): Long {
        return database.expensesDbQueries.franchiseBoutiqueInventoryCount().executeAsOne()
    }

    // --- SnackItemEntity ---
    fun insertSnackItem(franchiseId: Long, name: String, code: String, stock: Long, price: Double) {
        database.expensesDbQueries.snackItemCreate(franchiseId, name, code, stock, price)
    }

    fun getAllSnackItems(): List<SnackItemEntity> {
        return database.expensesDbQueries.snackItemSelectAll().executeAsList()
    }

    fun getSnackItemById(id: Long): SnackItemEntity? {
        return database.expensesDbQueries.snackItemSelectById(id).executeAsOneOrNull()
    }

    fun getSnackItemByCode(code: String): SnackItemEntity? {
        return database.expensesDbQueries.snackItemSelectByCode(code).executeAsOneOrNull()
    }

    fun updateSnackItem(id: Long, franchiseId: Long, name: String, code: String, stock: Long, price: Double) {
        database.expensesDbQueries.snackItemUpdate(franchiseId, name, code, stock, price, id)
    }

    fun deleteSnackItem(id: Long) {
        database.expensesDbQueries.snackItemDelete(id)
    }

    fun getSnackItemCount(): Long {
        return database.expensesDbQueries.snackItemCount().executeAsOne()
    }

    // --- PromotionEntity ---
    fun insertPromotion(name: String, startDate: String, endDate: String, discountType: String, discountValue: Double, applicableToNew: Long, applicableToActive: Long) {
        database.expensesDbQueries.promotionCreate(name, startDate, endDate, discountType, discountValue, applicableToNew, applicableToActive)
    }

    fun getAllPromotions(): List<PromotionEntity> {
        return database.expensesDbQueries.promotionSelectAll().executeAsList()
    }

    fun getPromotionById(id: Long): PromotionEntity? {
        return database.expensesDbQueries.promotionSelectById(id).executeAsOneOrNull()
    }

    fun updatePromotion(id: Long, name: String, startDate: String, endDate: String, discountType: String, discountValue: Double, applicableToNew: Long, applicableToActive: Long) {
        database.expensesDbQueries.promotionUpdate(name, startDate, endDate, discountType, discountValue, applicableToNew, applicableToActive, id)
    }

    fun deletePromotion(id: Long) {
        database.expensesDbQueries.promotionDelete(id)
    }

    fun getPromotionCount(): Long {
        return database.expensesDbQueries.promotionCount().executeAsOne()
    }

    // --- PaymentEntity ---
    fun insertPayment(studentId: Long, franchiseId: Long, amount: Double, paymentDate: String, paymentType: String, eventId: Long?, reference: String?) {
        database.expensesDbQueries.paymentCreate(studentId, franchiseId, amount, paymentDate, paymentType, eventId, reference)
    }

    fun getAllPayments(): List<PaymentEntity> {
        return database.expensesDbQueries.paymentSelectAll().executeAsList()
    }

    fun getPaymentById(id: Long): PaymentEntity? {
        return database.expensesDbQueries.paymentSelectById(id).executeAsOneOrNull()
    }

    fun getPaymentsByStudentId(studentId: Long): List<PaymentEntity> {
        return database.expensesDbQueries.paymentSelectByStudentId(studentId).executeAsList()
    }

    fun updatePayment(id: Long, studentId: Long, franchiseId: Long, amount: Double, paymentDate: String, paymentType: String, eventId: Long?, reference: String?) {
        database.expensesDbQueries.paymentUpdate(studentId, franchiseId, amount, paymentDate, paymentType, eventId, reference, id)
    }

    fun deletePayment(id: Long) {
        database.expensesDbQueries.paymentDelete(id)
    }

    fun getPaymentCount(): Long {
        return database.expensesDbQueries.paymentCount().executeAsOne()
    }

    // --- EventEntity ---
    fun insertEvent(name: String, description: String?, eventDate: String, type: String, cost: Double, ticketsAvailable: Long) {
        database.expensesDbQueries.eventCreate(name, description, eventDate, type, cost, ticketsAvailable)
    }

    fun getAllEvents(): List<EventEntity> {
        return database.expensesDbQueries.eventSelectAll().executeAsList()
    }

    fun getEventById(id: Long): EventEntity? {
        return database.expensesDbQueries.eventSelectById(id).executeAsOneOrNull()
    }

    fun updateEvent(id: Long, name: String, description: String?, eventDate: String, type: String, cost: Double, ticketsAvailable: Long) {
        database.expensesDbQueries.eventUpdate(name, description, eventDate, type, cost, ticketsAvailable, id)
    }

    fun deleteEvent(id: Long) {
        database.expensesDbQueries.eventDelete(id)
    }

    fun getEventCount(): Long {
        return database.expensesDbQueries.eventCount().executeAsOne()
    }

    // --- AdministrativeEntity ---
    fun insertAdministrative(
        franchiseId: Long, firstName: String, lastNamePaternal: String?, lastNameMaternal: String?,
        gender: String?, birthDate: String?, nationality: String?, taxId: String?, nss: String?,
        phone: String?, email: String?, addressStreet: String?, addressZip: String?,
        emergencyContactName: String?, emergencyContactPhone: String?, position: String,
        salary: Double, startDate: String, active: Long = 1
    ) {
        database.expensesDbQueries.administrativeCreate(
            franchiseId, firstName, lastNamePaternal, lastNameMaternal, gender, birthDate, nationality,
            taxId, nss, phone, email, addressStreet, addressZip, emergencyContactName,
            emergencyContactPhone, position, salary, startDate, active
        )
    }

    fun getAllAdministratives(): List<AdministrativeEntity> {
        return database.expensesDbQueries.administrativeSelectAll().executeAsList()
    }

    fun getAdministrativeById(id: Long): AdministrativeEntity? {
        return database.expensesDbQueries.administrativeSelectById(id).executeAsOneOrNull()
    }

    fun getAdministrativesByFranchiseId(franchiseId: Long): List<AdministrativeEntity> {
        return database.expensesDbQueries.administrativeSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateAdministrative(
        id: Long, franchiseId: Long, firstName: String, lastNamePaternal: String?, lastNameMaternal: String?,
        gender: String?, birthDate: String?, nationality: String?, taxId: String?, nss: String?,
        phone: String?, email: String?, addressStreet: String?, addressZip: String?,
        emergencyContactName: String?, emergencyContactPhone: String?, position: String,
        salary: Double, startDate: String, active: Long
    ) {
        database.expensesDbQueries.administrativeUpdate(
            franchiseId, firstName, lastNamePaternal, lastNameMaternal, gender, birthDate, nationality,
            taxId, nss, phone, email, addressStreet, addressZip, emergencyContactName,
            emergencyContactPhone, position, salary, startDate, active, id
        )
    }

    fun deleteAdministrative(id: Long) {
        database.expensesDbQueries.administrativeDelete(id)
    }

    fun getAdministrativeCount(): Long {
        return database.expensesDbQueries.administrativeCount().executeAsOne()
    }

    // --- TrialClassEntity ---
    fun insertTrialClass(
        franchiseId: Long, studentId: Long?, adultFirstName: String?, adultLastNamePaternal: String?,
        adultLastNameMaternal: String?, phone: String?, email: String?, studentFirstName: String,
        studentLastNamePaternal: String?, studentLastNameMaternal: String?, ageYears: Long,
        ageMonths: Long, disciplineId: Long, requestDate: String, scheduledDate: String?,
        scheduledTime: String?, classroomId: Long?, teacherId: Long?, attendance: Long?,
        cancellationReason: String?, howDiscovered: String?
    ) {
        database.expensesDbQueries.trialClassCreate(
            franchiseId, studentId, adultFirstName, adultLastNamePaternal, adultLastNameMaternal,
            phone, email, studentFirstName, studentLastNamePaternal, studentLastNameMaternal,
            ageYears, ageMonths, disciplineId, requestDate, scheduledDate, scheduledTime,
            classroomId, teacherId, attendance, cancellationReason, howDiscovered
        )
    }

    fun getAllTrialClasses(): List<TrialClassEntity> {
        return database.expensesDbQueries.trialClassSelectAll().executeAsList()
    }

    fun getTrialClassById(id: Long): TrialClassEntity? {
        return database.expensesDbQueries.trialClassSelectById(id).executeAsOneOrNull()
    }

    fun getTrialClassesByFranchiseId(franchiseId: Long): List<TrialClassEntity> {
        return database.expensesDbQueries.trialClassSelectByFranchiseId(franchiseId).executeAsList()
    }

    fun updateTrialClass(
        id: Long, franchiseId: Long, studentId: Long?, adultFirstName: String?, adultLastNamePaternal: String?,
        adultLastNameMaternal: String?, phone: String?, email: String?, studentFirstName: String,
        studentLastNamePaternal: String?, studentLastNameMaternal: String?, ageYears: Long,
        ageMonths: Long, disciplineId: Long, requestDate: String, scheduledDate: String?,
        scheduledTime: String?, classroomId: Long?, teacherId: Long?, attendance: Long?,
        cancellationReason: String?, howDiscovered: String?
    ) {
        database.expensesDbQueries.trialClassUpdate(
            franchiseId, studentId, adultFirstName, adultLastNamePaternal, adultLastNameMaternal,
            phone, email, studentFirstName, studentLastNamePaternal, studentLastNameMaternal,
            ageYears, ageMonths, disciplineId, requestDate, scheduledDate, scheduledTime,
            classroomId, teacherId, attendance, cancellationReason, howDiscovered, id
        )
    }

    fun deleteTrialClass(id: Long) {
        database.expensesDbQueries.trialClassDelete(id)
    }

    fun getTrialClassCount(): Long {
        return database.expensesDbQueries.trialClassCount().executeAsOne()
    }

    // --- TeacherReportEntity ---
    fun insertTeacherReport(teacherId: Long, franchiseId: Long, reportType: String, reportDate: String, observation: String?) {
        database.expensesDbQueries.teacherReportCreate(teacherId, franchiseId, reportType, reportDate, observation)
    }

    fun getAllTeacherReports(): List<TeacherReportEntity> {
        return database.expensesDbQueries.teacherReportSelectAll().executeAsList()
    }

    fun getTeacherReportById(id: Long): TeacherReportEntity? {
        return database.expensesDbQueries.teacherReportSelectById(id).executeAsOneOrNull()
    }

    fun getTeacherReportsByTeacherId(teacherId: Long): List<TeacherReportEntity> {
        return database.expensesDbQueries.teacherReportSelectByTeacherId(teacherId).executeAsList()
    }

    fun updateTeacherReport(id: Long, teacherId: Long, franchiseId: Long, reportType: String, reportDate: String, observation: String?) {
        database.expensesDbQueries.teacherReportUpdate(teacherId, franchiseId, reportType, reportDate, observation, id)
    }

    fun deleteTeacherReport(id: Long) {
        database.expensesDbQueries.teacherReportDelete(id)
    }

    fun getTeacherReportCount(): Long {
        return database.expensesDbQueries.teacherReportCount().executeAsOne()
    }

    // --- Otras consultas personalizadas ---
    fun getActiveBranchesCount(): Long {
        return database.expensesDbQueries.activeBranchesCount().executeAsOne()
    }

    fun getStudentsByGender(): Pair<Long, Long> {
        val students = database.expensesDbQueries.studentSelectAll().executeAsList()
        val maleCount = students.count { it.gender.equals("masculino", ignoreCase = true) }.toLong()
        val femaleCount = students.count { it.gender.equals("femenino", ignoreCase = true) }.toLong()
        return Pair(maleCount, femaleCount)
    }


    // --- PrecioBaseEntity ---
    fun insertPrecioBase(precio: Double) {
        database.expensesDbQueries.precioBaseCreate(precio)
    }

    fun getAllPreciosBase(): List<PrecioBaseEntity> {
        return database.expensesDbQueries.precioBaseSelectAll().executeAsList()
    }

    fun getPrecioBaseById(id: Long): PrecioBaseEntity? {
        return database.expensesDbQueries.precioBaseSelectById(id).executeAsOneOrNull()
    }

    fun updatePrecioBase(id: Long, precio: Double) {
        database.expensesDbQueries.precioBaseUpdate(precio, id)
    }

    fun deletePrecioBase(id: Long) {
        database.expensesDbQueries.precioBaseDelete(id)
    }

    fun getPrecioBaseCount(): Long {
        return database.expensesDbQueries.precioBaseCount().executeAsOne()
    }

    // Dentro de la clase Repository
    fun insertMembership(name: String, months_paid: Long, months_saved: Double) {
        database.expensesDbQueries.membershipCreate(name, months_paid, months_saved)
    }

    fun getAllMemberships(): List<MembershipEntity> {
        return database.expensesDbQueries.membershipSelectAll().executeAsList()
    }

    fun getMembershipById(id: Long): MembershipEntity? {
        return database.expensesDbQueries.membershipSelectById(id).executeAsOneOrNull()
    }

    fun updateMembership(id: Long, name: String, months_paid: Long, months_saved: Double) {
        database.expensesDbQueries.membershipUpdate(name, months_paid, months_saved, id)
    }

    fun deleteMembership(id: Long) {
        database.expensesDbQueries.membershipDelete(id)
    }

    fun getMembershipCount(): Long {
        return database.expensesDbQueries.membershipCount().executeAsOne()
    }
    fun initializeData() {
        val existingUsers = getUserCount()
        if (existingUsers == 0L) {
            insertUser(
                username = "Adminpresi12",
                password = "Adminpresi12",
                roleId = 1,
                franchiseId = 1
            )
        }
    }
}