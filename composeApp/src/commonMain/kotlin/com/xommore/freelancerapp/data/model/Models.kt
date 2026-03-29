package com.xommore.freelancerapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: String = "",
    val userId: String = "",
    val company: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val memo: String = "",
    val createdAt: Long = 0L
) {
    val displayName: String
        get() = company.ifBlank { name.ifBlank { "이름 없음" } }
}

@Serializable
data class Project(
    val id: String = "",
    val userId: String = "",

    val clientId: String? = null,
    val clientName: String = "",
    val clientCompany: String = "",
    val clientEmail: String = "",

    val startDate: Long = 0L,
    val endDate: Long = 0L,

    val brand: String = "",
    val workType: WorkType = WorkType.STYLING,
    val cuts: Int = 0,
    val basePrice: Long = 0,
    val extraCost: Long = 0,

    val propsHandling: Long = 0,
    val propsSummer: Long = 0,
    val propsKkojae: Long = 0,

    val status: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val paymentDate: Long? = null,
    val memo: String = "",
    val createdAt: Long = 0L
) {
    val date: Long get() = startDate
    val totalLabor: Long get() = (cuts * basePrice) + extraCost
    val totalProps: Long get() = propsHandling + propsSummer + propsKkojae
    val totalRevenue: Long get() = totalLabor
    val tax: Long get() = (totalLabor * 0.033).toLong()
    val netIncome: Long get() = totalLabor - tax
}

data class ProjectWithClient(
    val project: Project,
    val client: Client?
) {
    val displayCompany: String
        get() = client?.company ?: project.clientCompany
    val displayName: String
        get() = client?.name ?: project.clientName
    val displayEmail: String
        get() = client?.email ?: project.clientEmail
}

@Serializable
enum class WorkType(val displayName: String) {
    STYLING("제품 스타일링"),
    PLAN_STYLING("제품 기획+스타일링")
}

@Serializable
enum class ProjectStatus(val displayName: String) {
    IN_PROGRESS("진행중"),
    PENDING("입금예정"),
    PAID("입금완료")
}

@Serializable
data class UserProfile(
    val id: String = "default",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolder: String = "",
    val businessNumber: String = "",
    val address: String = "",
    val taxRate: Double = 3.3,
    val updatedAt: Long = 0L
)

@Serializable
data class PropItem(
    val id: String = "",
    val projectId: String = "",
    val name: String = "",
    val amount: Long = 0,
    val receiptUri: String? = null,
    val memo: String = "",
    val createdAt: Long = 0L
)

data class ProjectWithProps(
    val project: Project,
    val props: List<PropItem>
) {
    val totalPropsAmount: Long get() = props.sumOf { it.amount }
}