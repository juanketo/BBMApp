package org.example.appbbmges.data

import org.example.appbbmges.ClassAttendanceEntity
import org.example.appbbmges.ClassroomEntity
import org.example.appbbmges.DisciplineEntity
import org.example.appbbmges.DisciplineLevelGradeSelectAll
import org.example.appbbmges.FamilyEntity
import org.example.appbbmges.FranchiseDisciplineByFranchise
import org.example.appbbmges.FranchiseEntity
import org.example.appbbmges.GradeEntity
import org.example.appbbmges.LevelEntity
import org.example.appbbmges.RoleEntity
import org.example.appbbmges.ScheduleEntity
import org.example.appbbmges.ScheduleSelectFull
import org.example.appbbmges.StudentAttendanceByClass
import org.example.appbbmges.StudentAttendanceByStudent
import org.example.appbbmges.StudentEntity
import org.example.appbbmges.TeacherEntity
import org.example.appbbmges.TeacherHourlyRateEntity
import org.example.appbbmges.TeacherPaymentEntity
import org.example.appbbmges.UserEntity
import org.example.appbbmges.AppDatabaseBaby




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
    fun insertUser(
        username: String,
        passwordHash: String,
        active: Long = 1,
        createdAt: Long,
        updatedAt: Long
    ) {
        database.expensesDbQueries.userCreate(
            username,
            passwordHash,
            active,
            createdAt,
            updatedAt
        )
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

    // --- FranchiseEntity ---
    fun insertFranchise(
        name: String,
        email: String?,
        phone: String?,
        basePriceCents: Long,
        currency: String,
        taxName: String?,
        taxId: String?,
        zone: String?,
        isNew: Long = 0,
        active: Long = 1,
        addressId: Long?
    ) {
        database.expensesDbQueries.franchiseCreate(
            name,
            email,
            phone,
            basePriceCents,
            currency,
            taxName,
            taxId,
            zone,
            isNew,
            active,
            addressId
        )
    }

    fun getAllFranchises(): List<FranchiseEntity> {
        return database.expensesDbQueries.franchiseSelectAll().executeAsList()
    }

    fun getFranchiseById(id: Long): FranchiseEntity? {
        return database.expensesDbQueries.franchiseSelectById(id).executeAsOneOrNull()
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

    // --- GradeEntity ---
    fun insertGrade(name: String) {
        database.expensesDbQueries.gradeCreate(name)
    }

    fun getAllGrades(): List<GradeEntity> {
        return database.expensesDbQueries.gradeSelectAll().executeAsList()
    }

    fun getGradeById(id: Long): GradeEntity? {
        return database.expensesDbQueries.gradeSelectById(id).executeAsOneOrNull()
    }

    fun updateGrade(id: Long, name: String) {
        database.expensesDbQueries.gradeUpdate(name, id)
    }

    fun deleteGrade(id: Long) {
        database.expensesDbQueries.gradeDelete(id)
    }

    fun getGradeCount(): Long {
        return database.expensesDbQueries.gradeCount().executeAsOne()
    }

    // --- DisciplineEntity ---
    fun insertDiscipline(name: String) {
        database.expensesDbQueries.disciplineCreate(name)
    }

    fun getAllDisciplines(): List<DisciplineEntity> {
        return database.expensesDbQueries.disciplineSelectAll().executeAsList()
    }

    fun getDisciplineById(id: Long): DisciplineEntity? {
        return database.expensesDbQueries.disciplineSelectById(id).executeAsOneOrNull()
    }

    fun searchDisciplinesByBaseName(base: String): List<DisciplineEntity> {
        return database.expensesDbQueries.disciplineSearchByName(base).executeAsList()
    }

    fun updateDiscipline(id: Long, name: String) {
        database.expensesDbQueries.disciplineUpdate(name, id)
    }

    fun deleteDiscipline(id: Long) {
        database.expensesDbQueries.disciplineDelete(id)
    }

    fun getDisciplineCount(): Long {
        return database.expensesDbQueries.disciplineCount().executeAsOne()
    }

    // --- DisciplineLevelGradeEntity ---
    fun insertDisciplineLevelGrade(disciplineId: Long, levelId: Long, gradeId: Long) {
        database.expensesDbQueries.disciplineLevelGradeCreate(disciplineId, levelId, gradeId)
    }

    fun getAllDisciplineLevelGrades(): List<DisciplineLevelGradeSelectAll> {
        return database.expensesDbQueries.disciplineLevelGradeSelectAll().executeAsList()
    }

    fun getLevelsByDiscipline(disciplineId: Long): List<LevelEntity> {
        return database.expensesDbQueries.levelsByDiscipline(disciplineId).executeAsList()
    }

    fun getGradesByDisciplineAndLevel(disciplineId: Long, levelId: Long): List<GradeEntity> {
        return database.expensesDbQueries.gradesByDisciplineAndLevel(disciplineId, levelId)
            .executeAsList()
    }

    fun deleteDisciplineLevelGrade(disciplineId: Long, levelId: Long, gradeId: Long) {
        database.expensesDbQueries.disciplineLevelGradeDelete(disciplineId, levelId, gradeId)
    }

    fun getDisciplineLevelGradeCount(): Long {
        return database.expensesDbQueries.disciplineLevelGradeCount().executeAsOne()
    }

    // --- FranchiseDisciplineEntity ---
    fun insertFranchiseDiscipline(franchiseId: Long, disciplineLevelGradeId: Long) {
        database.expensesDbQueries.franchiseDisciplineCreate(franchiseId, disciplineLevelGradeId)
    }

    fun getFranchiseDisciplines(franchiseId: Long): List<FranchiseDisciplineByFranchise> {
        return database.expensesDbQueries.franchiseDisciplineByFranchise(franchiseId)
            .executeAsList()
    }

    fun deleteFranchiseDiscipline(franchiseId: Long, disciplineLevelGradeId: Long) {
        database.expensesDbQueries.franchiseDisciplineDelete(franchiseId, disciplineLevelGradeId)
    }

    fun getFranchiseDisciplineCount(franchiseId: Long): Long {
        return database.expensesDbQueries.franchiseDisciplineCount(franchiseId).executeAsOne()
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
        return database.expensesDbQueries.classroomByFranchise(franchiseId).executeAsList()
    }

    fun updateClassroom(id: Long, franchiseId: Long, name: String) {
        database.expensesDbQueries.classroomUpdate(franchiseId, name, id)
    }

    fun deleteClassroom(id: Long) {
        database.expensesDbQueries.classroomDelete(id)
    }
    fun roleCreate(name: String, description: String?) {
        database.expensesDbQueries.roleCreate(name, description)
    }

    // --- TeacherEntity ---
    fun insertTeacher(
        userId: Long,
        firstName: String,
        lastNamePaternal: String?,
        lastNameMaternal: String?,
        phone: String?,
        email: String?,
        rfc: String?,
        startTs: Long,
        active: Long = 1,
        vetoed: Long = 0
    ) {
        database.expensesDbQueries.teacherCreate(
            userId,
            firstName,
            lastNamePaternal,
            lastNameMaternal,
            phone,
            email,
            rfc,
            startTs,
            active,
            vetoed
        )
    }

    fun getAllTeachers(): List<TeacherEntity> {
        return database.expensesDbQueries.teacherSelectAll().executeAsList()
    }

    fun getTeacherById(id: Long): TeacherEntity? {
        return database.expensesDbQueries.teacherSelectById(id).executeAsOneOrNull()
    }

    fun updateTeacher(
        id: Long,
        userId: Long,
        firstName: String,
        lastNamePaternal: String?,
        lastNameMaternal: String?,
        phone: String?,
        email: String?,
        rfc: String?,
        startTs: Long,
        active: Long,
        vetoed: Long
    ) {
        database.expensesDbQueries.teacherUpdate(
            userId,
            firstName,
            lastNamePaternal,
            lastNameMaternal,
            phone,
            email,
            rfc,
            startTs,
            active,
            vetoed,
            id
        )
    }

    fun deleteTeacher(id: Long) {
        database.expensesDbQueries.teacherDelete(id)
    }

    fun getTeacherCount(): Long {
        return database.expensesDbQueries.teacherCount().executeAsOne()
    }

    // --- ScheduleEntity ---
    fun insertSchedule(
        franchiseId: Long,
        classroomId: Long,
        teacherId: Long,
        disciplineLevelGradeId: Long,
        dayOfWeek: Int,
        startMinutes: Int,
        endMinutes: Int
    ) {
        database.expensesDbQueries.scheduleCreate(
            franchiseId,
            classroomId,
            teacherId,
            disciplineLevelGradeId,
            dayOfWeek,
            startMinutes,
            endMinutes
        )
    }

    fun getSchedulesByFranchise(franchiseId: Long): List<ScheduleEntity> {
        return database.expensesDbQueries.schedulesByFranchise(franchiseId).executeAsList()
    }

    fun getSchedulesByTeacher(teacherId: Long): List<ScheduleEntity> {
        return database.expensesDbQueries.schedulesByTeacher(teacherId).executeAsList()
    }

    fun getFullSchedules(): List<ScheduleSelectFull> {
        return database.expensesDbQueries.scheduleSelectFull().executeAsList()
    }

    fun deleteSchedule(id: Long) {
        database.expensesDbQueries.scheduleDelete(id)
    }

    // --- StudentEntity ---
    fun insertStudent(
        userId: Long?,
        franchiseId: Long,
        firstName: String,
        lastNamePaternal: String?,
        lastNameMaternal: String?,
        birthDateTs: Long?,
        curp: String?,
        phone: String?,
        email: String?,
        addressId: Long?,
        active: Long = 1
    ) {
        database.expensesDbQueries.studentCreate(
            userId,
            franchiseId,
            firstName,
            lastNamePaternal,
            lastNameMaternal,
            birthDateTs,
            curp,
            phone,
            email,
            addressId,
            active
        )
    }

    fun getAllStudents(): List<StudentEntity> {
        return database.expensesDbQueries.selectAllStudents().executeAsList()
    }

    fun getStudentById(id: Long): StudentEntity? {
        return database.expensesDbQueries.selectStudentById(id).executeAsOneOrNull()
    }

    fun deleteStudent(id: Long) {
        database.expensesDbQueries.studentDelete(id)
    }

    fun getStudentCount(): Long {
        return database.expensesDbQueries.studentCount().executeAsOne()
    }

    // --- StudentScheduleEntity ---
    fun insertStudentSchedule(studentId: Long, scheduleId: Long) {
        database.expensesDbQueries.studentScheduleCreate(studentId, scheduleId)
    }

    fun getStudentSchedules(studentId: Long): List<ScheduleEntity> {
        return database.expensesDbQueries.studentSchedules(studentId).executeAsList()
    }

    fun getStudentsBySchedule(scheduleId: Long): List<StudentEntity> {
        return database.expensesDbQueries.studentsBySchedule(scheduleId).executeAsList()
    }

    fun deleteStudentSchedule(studentId: Long, scheduleId: Long) {
        database.expensesDbQueries.studentScheduleDelete(studentId, scheduleId)
    }

    fun getStudentScheduleCount(studentId: Long): Long {
        return database.expensesDbQueries.studentScheduleCount(studentId).executeAsOne()
    }

    // --- ClassAttendanceEntity ---
    fun insertClassAttendance(
        scheduleId: Long,
        teacherId: Long,
        dateTs: Long,
        durationMinutes: Int,
        validated: Long = 0
    ) {
        database.expensesDbQueries.insertClassAttendance(
            scheduleId,
            teacherId,
            dateTs,
            durationMinutes,
            validated
        )
    }

    fun getAttendanceByTeacherAndPeriod(
        teacherId: Long,
        startDateTs: Long,
        endDateTs: Long
    ): List<ClassAttendanceEntity> {
        return database.expensesDbQueries.getAttendanceByTeacherAndPeriod(
            teacherId,
            startDateTs,
            endDateTs
        ).executeAsList()
    }

    fun validateClassAttendance(id: Long) {
        database.expensesDbQueries.validateClassAttendance(id)
    }

    fun deleteClassAttendance(id: Long) {
        database.expensesDbQueries.deleteClassAttendance(id)
    }

    fun countTeacherAttendance(
        teacherId: Long,
        startDateTs: Long,
        endDateTs: Long
    ): Long {
        return database.expensesDbQueries.countTeacherAttendance(
            teacherId,
            startDateTs,
            endDateTs
        ).executeAsOne()
    }

    // --- StudentClassAttendanceEntity ---
    fun insertStudentAttendance(
        studentId: Long,
        classAttendanceId: Long,
        attended: Long = 1
    ) {
        database.expensesDbQueries.studentAttendanceCreate(
            studentId,
            classAttendanceId,
            attended
        )
    }

    fun getStudentAttendance(studentId: Long): List<StudentAttendanceByStudent> {
        return database.expensesDbQueries.studentAttendanceByStudent(studentId).executeAsList()
    }

    fun getStudentAttendanceByClass(classAttendanceId: Long): List<StudentAttendanceByClass> {
        return database.expensesDbQueries.studentAttendanceByClass(classAttendanceId)
            .executeAsList()
    }

    fun updateStudentAttendance(
        studentId: Long,
        classAttendanceId: Long,
        attended: Long
    ) {
        database.expensesDbQueries.studentAttendanceUpdate(
            attended,
            studentId,
            classAttendanceId
        )
    }

    fun deleteStudentAttendance(studentId: Long, classAttendanceId: Long) {
        database.expensesDbQueries.studentAttendanceDelete(studentId, classAttendanceId)
    }

    fun countStudentAttendances(studentId: Long): Long {
        return database.expensesDbQueries.studentAttendanceCount(studentId).executeAsOne()
    }

    // --- TeacherPaymentEntity ---
    fun insertTeacherPayment(
        teacherId: Long,
        franchiseId: Long,
        totalMinutes: Int,
        rateCents: Int,
        totalPaidCents: Int,
        periodLabel: String,
        paidTs: Long,
        notes: String?
    ) {
        database.expensesDbQueries.insertTeacherPayment(
            teacherId,
            franchiseId,
            totalMinutes,
            rateCents,
            totalPaidCents,
            periodLabel,
            paidTs,
            notes
        )
    }

    fun getPaymentsByTeacher(teacherId: Long): List<TeacherPaymentEntity> {
        return database.expensesDbQueries.getPaymentsByTeacher(teacherId).executeAsList()
    }

    fun getPaymentsByFranchise(franchiseId: Long): List<TeacherPaymentEntity> {
        return database.expensesDbQueries.getPaymentsByFranchise(franchiseId).executeAsList()
    }

    fun getPaymentById(id: Long): TeacherPaymentEntity? {
        return database.expensesDbQueries.getPaymentById(id).executeAsOneOrNull()
    }

    fun deleteTeacherPayment(id: Long) {
        database.expensesDbQueries.deleteTeacherPayment(id)
    }

    // --- TeacherHourlyRateEntity ---
    fun insertTeacherHourlyRate(
        teacherId: Long,
        franchiseId: Long,
        rateCents: Int,
        active: Long = 1,
        createdTs: Long
    ) {
        database.expensesDbQueries.insertTeacherHourlyRate(
            teacherId,
            franchiseId,
            rateCents,
            active,
            createdTs
        )
    }

    fun getActiveHourlyRateByTeacher(
        teacherId: Long,
        franchiseId: Long
    ): TeacherHourlyRateEntity? {
        return database.expensesDbQueries.getActiveHourlyRateByTeacher(
            teacherId,
            franchiseId
        ).executeAsOneOrNull()
    }

    fun deactivateTeacherRates(teacherId: Long, franchiseId: Long) {
        database.expensesDbQueries.deactivateTeacherRates(teacherId, franchiseId)
    }

    fun getHourlyRateHistoryByTeacher(teacherId: Long): List<TeacherHourlyRateEntity> {
        return database.expensesDbQueries.getHourlyRateHistoryByTeacher(teacherId).executeAsList()
    }

    // --- FamilyEntity ---
    fun createFamily(name: String, franchiseId: Long) {
        database.expensesDbQueries.createFamily(name, franchiseId)
    }

    fun getAllFamilies(): List<FamilyEntity> {
        return database.expensesDbQueries.getAllFamilies().executeAsList()
    }

    fun getFamilyById(id: Long): FamilyEntity? {
        return database.expensesDbQueries.getFamilyById(id).executeAsOneOrNull()
    }

    fun updateFamily(id: Long, name: String, franchiseId: Long) {
        database.expensesDbQueries.updateFamily(name, franchiseId, id)
    }

    fun deleteFamily(id: Long) {
        database.expensesDbQueries.deleteFamily(id)
    }

    fun countFamiliesByFranchise(franchiseId: Long): Long {
        return database.expensesDbQueries.countFamiliesByFranchise(franchiseId).executeAsOne()
    }

    // --- StudentFamilyEntity ---
    fun assignStudentToFamily(studentId: Long, familyId: Long) {
        database.expensesDbQueries.assignStudentToFamily(studentId, familyId)
    }

    fun getStudentsByFamily(familyId: Long): List<StudentEntity> {
        return database.expensesDbQueries.getStudentsByFamily(familyId).executeAsList()
    }

    fun getFamilyByStudent(studentId: Long): FamilyEntity? {
        return database.expensesDbQueries.getFamilyByStudent(studentId).executeAsOneOrNull()
    }

    fun deleteStudentFromFamily(studentId: Long, familyId: Long) {
        database.expensesDbQueries.deleteStudentFamily(studentId, familyId)
    }

    fun deleteAllStudentsFromFamily(familyId: Long) {
        database.expensesDbQueries.deleteAllStudentsFromFamily(familyId)
    }

    // --- MonthlyPaymentEntity ---
    fun insertMonthlyPayment(
        studentId: Long,
        franchiseId: Long,
        period: String,
        dueTs: Long,
        paidTs: Long?,
        amountCents: Int,
        status: String
    ) {
        database.expensesDbQueries.monthlyPaymentCreate(
            studentId,
            franchiseId,
            period,
            dueTs,
            paidTs,
            amountCents,
            status
        )
    }

    fun getAllMonthlyPayments(): List<MonthlyPaymentEntity> {
        return database.expensesDbQueries.monthlyPaymentSelectAll().executeAsList()
    }

    fun getMonthlyPaymentById(id: Long): MonthlyPaymentEntity? {
        return database.expensesDbQueries.monthlyPaymentSelectById(id).executeAsOneOrNull()
    }

    fun getMonthlyPaymentsByStudent(studentId: Long): List<MonthlyPaymentEntity> {
        return database.expensesDbQueries.monthlyPaymentSelectByStudentId(studentId).executeAsList()
    }

    fun getMonthlyPaymentsByFranchiseAndPeriod(
        franchiseId: Long,
        period: String
    ): List<MonthlyPaymentEntity> {
        return database.expensesDbQueries.monthlyPaymentSelectByFranchiseAndPeriod(
            franchiseId,
            period
        ).executeAsList()
    }

    fun updateMonthlyPayment(
        id: Long,
        studentId: Long,
        franchiseId: Long,
        period: String,
        dueTs: Long,
        paidTs: Long?,
        amountCents: Int,
        status: String
    ) {
        database.expensesDbQueries.monthlyPaymentUpdate(
            studentId,
            franchiseId,
            period,
            dueTs,
            paidTs,
            amountCents,
            status,
            id
        )
    }

    fun deleteMonthlyPayment(id: Long) {
        database.expensesDbQueries.monthlyPaymentDelete(id)
    }

    fun countPaidMonthlyPayments(studentId: Long): Long {
        return database.expensesDbQueries.monthlyPaymentCountPaid(studentId).executeAsOne()
    }

    // --- MonthlyPaymentDiscountEntity ---
    fun insertMonthlyPaymentDiscount(
        monthlyPaymentId: Long,
        label: String,
        amountCents: Int
    ) {
        database.expensesDbQueries.monthlyPaymentDiscountCreate(
            monthlyPaymentId,
            label,
            amountCents
        )
    }

    fun getDiscountsByMonthlyPayment(monthlyPaymentId: Long): List<MonthlyPaymentDiscountEntity> {
        return database.expensesDbQueries
            .monthlyPaymentDiscountSelectByPaymentId(monthlyPaymentId)
            .executeAsList()
    }

    fun deleteDiscountsByMonthlyPayment(monthlyPaymentId: Long) {
        database.expensesDbQueries
            .monthlyPaymentDiscountDeleteByPaymentId(monthlyPaymentId)
    }

    // --- MonthlyPaymentNoteEntity ---
    fun insertMonthlyPaymentNote(
        monthlyPaymentId: Long,
        content: String
    ) {
        database.expensesDbQueries.monthlyPaymentNoteCreate(
            monthlyPaymentId,
            content
        )
    }

    fun getNotesByMonthlyPayment(monthlyPaymentId: Long): List<MonthlyPaymentNoteEntity> {
        return database.expensesDbQueries
            .monthlyPaymentNoteSelectByPaymentId(monthlyPaymentId)
            .executeAsList()
    }

    fun deleteNotesByMonthlyPayment(monthlyPaymentId: Long) {
        database.expensesDbQueries
            .monthlyPaymentNoteDeleteByPaymentId(monthlyPaymentId)
    }

    // --- MonthlyPaymentPartialEntity ---
    fun insertMonthlyPaymentPartial(
        monthlyPaymentId: Long,
        amountCents: Int,
        paidTs: Long
    ) {
        database.expensesDbQueries.monthlyPaymentPartialCreate(
            monthlyPaymentId,
            amountCents,
            paidTs
        )
    }

    fun getPartialsByMonthlyPayment(monthlyPaymentId: Long): List<MonthlyPaymentPartialEntity> {
        return database.expensesDbQueries
            .monthlyPaymentPartialSelectByPaymentId(monthlyPaymentId)
            .executeAsList()
    }

    fun deletePartialsByMonthlyPayment(monthlyPaymentId: Long) {
        database.expensesDbQueries
            .monthlyPaymentPartialDeleteByPaymentId(monthlyPaymentId)
    }

    // --- MonthlyPaymentStatusEntity ---
    fun insertMonthlyPaymentStatus(code: String, label: String) {
        database.expensesDbQueries.monthlyPaymentStatusCreate(code, label)
    }

    fun getAllMonthlyPaymentStatuses(): List<MonthlyPaymentStatusEntity> {
        return database.expensesDbQueries.monthlyPaymentStatusSelectAll().executeAsList()
    }

    fun getMonthlyPaymentStatusByCode(code: String): MonthlyPaymentStatusEntity? {
        return database.expensesDbQueries
            .monthlyPaymentStatusSelectByCode(code)
            .executeAsOneOrNull()
    }

    fun updateMonthlyPaymentStatus(code: String, label: String) {
        database.expensesDbQueries.monthlyPaymentStatusUpdate(label, code)
    }

    fun deleteMonthlyPaymentStatus(code: String) {
        database.expensesDbQueries.monthlyPaymentStatusDelete(code)
    }

    // --- StudentDebtEntity ---
    fun insertStudentDebt(
        studentId: Long,
        concept: String,
        amountCents: Int,
        dueTs: Long,
        resolved: Long = 0
    ) {
        database.expensesDbQueries.studentDebtCreate(
            studentId, concept, amountCents, dueTs, resolved
        )
    }

    fun getAllStudentDebts(): List<StudentDebtEntity> {
        return database.expensesDbQueries.studentDebtSelectAll().executeAsList()
    }

    fun getStudentDebtsByStudentId(studentId: Long): List<StudentDebtEntity> {
        return database.expensesDbQueries.studentDebtSelectByStudentId(studentId).executeAsList()
    }

    fun markStudentDebtAsResolved(id: Long) {
        database.expensesDbQueries.studentDebtMarkResolved(id)
    }

    fun deleteStudentDebt(id: Long) {
        database.expensesDbQueries.studentDebtDelete(id)
    }

    // --- FamilyEntity ---
    fun insertFamily(
        name: String,
        phone: String?,
        email: String?
    ) {
        database.expensesDbQueries.familyCreate(name, phone, email)
    }

    fun getAllFamilies(): List<FamilyEntity> {
        return database.expensesDbQueries.familySelectAll().executeAsList()
    }

    fun getFamilyById(id: Long): FamilyEntity? {
        return database.expensesDbQueries.familySelectById(id).executeAsOneOrNull()
    }

    fun updateFamily(id: Long, name: String, phone: String?, email: String?) {
        database.expensesDbQueries.familyUpdate(name, phone, email, id)
    }

    fun deleteFamily(id: Long) {
        database.expensesDbQueries.familyDelete(id)
    }

    fun getFamilyCount(): Long {
        return database.expensesDbQueries.familyCount().executeAsOne()
    }

    fun initializeData() {
        val currentTime = System.currentTimeMillis()

        if (getUserCount() == 0L) {
            insertUser(
                username = "Adminpresi12",
                passwordHash = "Adminpresi12",
                active = 1,
                createdAt = currentTime,
                updatedAt = currentTime
            )

            val user = getUserByUsername("Adminpresi12")
            val adminRole = getAllRoles().firstOrNull { it.name == "ADMIN" }
            val franchise = getAllFranchises().first()

            if (user != null && adminRole != null) {
            }
        }
    }
}