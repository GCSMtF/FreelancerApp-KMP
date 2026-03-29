package com.xommore.freelancerapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.xommore.freelancerapp.data.model.*
import com.xommore.freelancerapp.db.FreelancerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val db: FreelancerDatabase) {

    private val queries = db.freelancerDatabaseQueries

    fun getProjects(userId: String): Flow<List<Project>> {
        return queries.getProjectsByUser(userId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toProject() } }
    }

    fun insertProject(project: Project) {
        queries.insertProject(
            id = project.id,
            userId = project.userId,
            clientId = project.clientId,
            clientName = project.clientName,
            clientCompany = project.clientCompany,
            clientEmail = project.clientEmail,
            startDate = project.startDate,
            endDate = project.endDate,
            brand = project.brand,
            workType = project.workType.name,
            cuts = project.cuts.toLong(),
            basePrice = project.basePrice,
            extraCost = project.extraCost,
            propsHandling = project.propsHandling,
            propsSummer = project.propsSummer,
            propsKkojae = project.propsKkojae,
            status = project.status.name,
            paymentDate = project.paymentDate,
            memo = project.memo,
            createdAt = project.createdAt
        )
    }

    fun deleteProject(projectId: String) {
        queries.deleteProjectById(projectId)
    }

    fun getProjectCount(userId: String): Long {
        return queries.getProjectCount(userId).executeAsOne()
    }
}

// SQLDelight 생성 클래스 → 앱 모델 변환
private fun com.xommore.freelancerapp.db.Projects.toProject(): Project {
    return Project(
        id = id,
        userId = userId,
        clientId = clientId,
        clientName = clientName,
        clientCompany = clientCompany,
        clientEmail = clientEmail,
        startDate = startDate,
        endDate = endDate,
        brand = brand,
        workType = try { WorkType.valueOf(workType) } catch (e: Exception) { WorkType.STYLING },
        cuts = cuts.toInt(),
        basePrice = basePrice,
        extraCost = extraCost,
        propsHandling = propsHandling,
        propsSummer = propsSummer,
        propsKkojae = propsKkojae,
        status = try { ProjectStatus.valueOf(status) } catch (e: Exception) { ProjectStatus.IN_PROGRESS },
        paymentDate = paymentDate,
        memo = memo,
        createdAt = createdAt
    )
}