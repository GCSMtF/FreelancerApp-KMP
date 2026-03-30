package com.xommore.freelancerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xommore.freelancerapp.data.currentYear
import com.xommore.freelancerapp.data.currentMonth
import com.xommore.freelancerapp.data.getYear
import com.xommore.freelancerapp.data.getMonth
import com.xommore.freelancerapp.data.model.*
import com.xommore.freelancerapp.data.repository.ClientRepository
import com.xommore.freelancerapp.data.repository.ProjectRepository
import com.xommore.freelancerapp.data.repository.PropItemRepository
import com.xommore.freelancerapp.data.repository.UserProfileRepository
import com.xommore.freelancerapp.db.FreelancerDatabase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(database: FreelancerDatabase) : ViewModel() {

    private val projectRepository = ProjectRepository(database)
    private val clientRepository = ClientRepository(database)
    private val userProfileRepository = UserProfileRepository(database)
    private val propItemRepository = PropItemRepository(database)

    private val currentUserId: String
        get() = Firebase.auth.currentUser?.uid ?: ""

    // 선택된 연도/월
    private val _selectedYear = MutableStateFlow(currentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 전체 프로젝트 목록
    private val allProjectsFlow: Flow<List<Project>> = flow {
        if (currentUserId.isNotEmpty()) {
            emitAll(projectRepository.getProjects(currentUserId))
        } else {
            emit(emptyList())
        }
    }

    // 프로젝트 목록 (월별 필터링)
    val projects: StateFlow<List<Project>> = combine(
        allProjectsFlow,
        _selectedYear,
        _selectedMonth
    ) { projectList, year, month ->
        projectList.filter { project ->
            val projectYear = getYear(project.startDate)
            val projectMonth = getMonth(project.startDate)
            projectYear == year && projectMonth == month
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 전체 프로젝트 (연간 통계용)
    val allProjects: StateFlow<List<Project>> = combine(
        allProjectsFlow,
        _selectedYear
    ) { projectList, year ->
        projectList.filter { project ->
            getYear(project.startDate) == year
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 클라이언트 목록
    val clients: StateFlow<List<Client>> = flow {
        if (currentUserId.isNotEmpty()) {
            emitAll(clientRepository.getClients(currentUserId))
        } else {
            emit(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 프로젝트 + 클라이언트 정보
    val projectsWithClients: StateFlow<List<ProjectWithClient>> = combine(
        projects,
        clients
    ) { projectList, clientList ->
        val clientMap = clientList.associateBy { it.id }
        projectList.map { project ->
            ProjectWithClient(
                project = project,
                client = project.clientId?.let { clientMap[it] }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 사용자 프로필
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    // =====================================================
    // 연도/월 선택
    // =====================================================

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun selectMonth(month: Int) {
        _selectedMonth.value = month
    }

    // =====================================================
    // 프로젝트 CRUD
    // =====================================================

    fun addProject(project: Project) {
        viewModelScope.launch {
            val projectWithUserId = project.copy(userId = currentUserId)
            projectRepository.insertProject(projectWithUserId)
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            projectRepository.insertProject(project)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
        }
    }

    fun updateProjectStatus(projectId: String, status: ProjectStatus) {
        viewModelScope.launch {
            val project = projects.value.find { it.id == projectId }
                ?: allProjects.value.find { it.id == projectId }
            project?.let {
                projectRepository.insertProject(it.copy(status = status))
            }
        }
    }

    // =====================================================
    // 클라이언트 CRUD
    // =====================================================

    fun addClient(client: Client) {
        viewModelScope.launch {
            val clientWithUserId = client.copy(userId = currentUserId)
            clientRepository.insertClient(clientWithUserId)
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            clientRepository.updateClient(client)
        }
    }

    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            clientRepository.deleteClient(clientId)
        }
    }

    // =====================================================
    // 사용자 프로필
    // =====================================================

    private fun loadUserProfile() {
        viewModelScope.launch {
            if (currentUserId.isNotEmpty()) {
                userProfileRepository.getProfile(currentUserId).collect { profile ->
                    _userProfile.value = profile
                }
            }
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            val profileWithUserId = profile.copy(userId = currentUserId)
            userProfileRepository.saveProfile(profileWithUserId)
        }
    }

    // =====================================================
    // 소품비(PropItem) CRUD
    // =====================================================

    fun getPropsForProject(projectId: String): Flow<List<PropItem>> {
        return propItemRepository.getPropsForProject(projectId)
    }

    fun getPropsForProjectOnce(projectId: String): List<PropItem> {
        return propItemRepository.getPropsForProjectOnce(projectId)
    }

    fun addPropItem(propItem: PropItem) {
        viewModelScope.launch {
            propItemRepository.insertPropItem(propItem)
        }
    }

    fun addPropItems(propItems: List<PropItem>) {
        viewModelScope.launch {
            propItemRepository.insertPropItems(propItems)
        }
    }

    fun deletePropItemById(propId: String) {
        viewModelScope.launch {
            propItemRepository.deletePropItemById(propId)
        }
    }

    fun deleteAllPropsForProject(projectId: String) {
        viewModelScope.launch {
            propItemRepository.deleteAllPropsForProject(projectId)
        }
    }

    // =====================================================
    // 프로젝트 + 소품비 함께 저장
    // =====================================================

    fun saveProjectWithProps(project: Project, propItems: List<PropItem>) {
        viewModelScope.launch {
            val projectWithUserId = project.copy(userId = currentUserId)
            projectRepository.insertProject(projectWithUserId)
            val propsWithProjectId = propItems.map { it.copy(projectId = project.id) }
            propItemRepository.insertPropItems(propsWithProjectId)
        }
    }

    fun updateProjectWithProps(project: Project, propItems: List<PropItem>) {
        viewModelScope.launch {
            projectRepository.insertProject(project)
            propItemRepository.deleteAllPropsForProject(project.id)
            val propsWithProjectId = propItems.map { it.copy(projectId = project.id) }
            propItemRepository.insertPropItems(propsWithProjectId)
        }
    }

    // =====================================================
    // 세율 계산 헬퍼
    // =====================================================

    private fun getTaxRate(): Double {
        return (_userProfile.value?.taxRate ?: 3.3) / 100.0
    }

    fun calculateTax(project: Project): Long {
        return (project.totalLabor * getTaxRate()).toLong()
    }

    fun calculateNetIncome(project: Project): Long {
        return project.totalLabor - calculateTax(project)
    }

    // =====================================================
    // 통계 관련
    // =====================================================

    fun getMonthlyRevenue(): Long = projects.value.sumOf { it.totalLabor }
    fun getMonthlyTax(): Long = projects.value.sumOf { calculateTax(it) }
    fun getMonthlyNetIncome(): Long = projects.value.sumOf { calculateNetIncome(it) }
    fun getMonthlyProps(): Long = projects.value.sumOf { it.totalProps }
    fun getTotalProjectCount(): Int = projects.value.size
    fun getPaidProjectCount(): Int = projects.value.count { it.status == ProjectStatus.PAID }
    fun getYearlyNetIncome(): Long = allProjects.value.sumOf { calculateNetIncome(it) }

    fun getMonthlyRevenueData(): List<Pair<String, Long>> {
        val months = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
        val year = _selectedYear.value

        return months.mapIndexed { index, monthName ->
            val month = index + 1
            val monthProjects = allProjects.value.filter { project ->
                getYear(project.startDate) == year && getMonth(project.startDate) == month
            }
            monthName to monthProjects.sumOf { calculateNetIncome(it) }
        }
    }

    fun getBrandRevenue(): Map<String, Long> {
        return projects.value
            .groupBy { it.brand }
            .mapValues { (_, projects) -> projects.sumOf { calculateNetIncome(it) } }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    // =====================================================
    // 백업 복원 / 데이터 초기화
    // =====================================================

    fun restoreMerge(
        restoredProjects: List<Project>,
        restoredClients: List<Client>,
        restoredProfile: UserProfile?
    ) {
        viewModelScope.launch {
            restoredProjects.forEach { project ->
                projectRepository.insertProject(project.copy(userId = currentUserId))
            }
            restoredClients.forEach { client ->
                clientRepository.insertClient(client.copy(userId = currentUserId))
            }
            if (_userProfile.value == null && restoredProfile != null) {
                userProfileRepository.saveProfile(restoredProfile.copy(userId = currentUserId))
            }
        }
    }

    fun restoreOverwrite(
        restoredProjects: List<Project>,
        restoredClients: List<Client>,
        restoredProfile: UserProfile?
    ) {
        viewModelScope.launch {
            clearAllData()
            restoredProjects.forEach { project ->
                projectRepository.insertProject(project.copy(userId = currentUserId))
            }
            restoredClients.forEach { client ->
                clientRepository.insertClient(client.copy(userId = currentUserId))
            }
            restoredProfile?.let {
                userProfileRepository.saveProfile(it.copy(userId = currentUserId))
            }
        }
    }
    /**
     * 여러 프로젝트의 소품비를 한번에 조회
     */
    suspend fun getPropsForProjects(projectIds: List<String>): Map<String, List<PropItem>> {
        return projectIds.associateWith { projectId ->
            propItemRepository.getPropsForProjectOnce(projectId)
        }
    }
    fun clearAllData() {
        viewModelScope.launch {
            projects.value.forEach { projectRepository.deleteProject(it.id) }
            allProjects.value.forEach { projectRepository.deleteProject(it.id) }
            clients.value.forEach { clientRepository.deleteClient(it.id) }
            if (currentUserId.isNotEmpty()) {
                userProfileRepository.deleteProfile(currentUserId)
            }
        }
    }
}