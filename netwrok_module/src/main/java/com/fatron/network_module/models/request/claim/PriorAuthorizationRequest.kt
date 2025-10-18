package com.fatron.network_module.models.request.claim


import com.fatron.network_module.models.response.Scribe.User
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.walkinpharmacy.FamilyMember


import com.squareup.moshi.Json

data class PriorAuthorizationRequest(
    @Json(name = "prior_auth_id") val priorAuthId: String?,
    @Json(name = "id") val id: String?,
    @Json(name = "family_member_id") val familyMemberId: String?,
    @Json(name = "patient_name") val patientName: String?,
    @Json(name = "patient_age") val patientAge: String?,
    @Json(name = "employee_name") val employeeName: String?,
    @Json(name = "employee_cnic") val employeeCnic: String?,
    @Json(name = "company_name") val companyName: String?,
    @Json(name = "policy_number") val policyNumber: String?,
    @Json(name = "admission_date") val admissionDate: String?,
    @Json(name = "hospital_record_number") val hospitalRecordNumber: String?,
    @Json(name = "surgeon_name") val surgeonName: String?,
    @Json(name = "medical_history") val medicalHistory: String?,
    @Json(name = "diagnosis") val diagnosis: String?,
    @Json(name = "disease_disorder") val diseaseDisorder: String?,
    @Json(name = "nature_of_surgery") val natureOfSurgery: String?,
    @Json(name = "stay_length") val stayLength: String?,
    @Json(name = "estimated_cost") val estimatedCost: String?,
    @Json(name = "attending_doctor") val attendingDoctor: String?,
    @Json(name = "unique_identification_number") val uniqueIdentificationNumber: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "presenting_complaints") val presentingComplaints: String?,
)

data class priorAuthDetails(
    @Json(name = "id") val id: Int?=null,
    @Json(name = "type_id") val typeId: Int?=null,
    @Json(name = "user_id") val userId: Int?=null,
    @Json(name = "family_member_id") val familyMemberId: Int?=null,
    @Json(name = "patient_name")  val patientName: String?=null,
    @Json(name = "patient_age") val patientAge: Int?=null,
    @Json(name = "employee_name")  val employeeName: String?=null,
    @Json(name = "employee_cnic") val employeeCnic: String?=null,
    @Json(name = "company_name") val companyName: String?=null,
    @Json(name = "policy_number") val policyNumber: String?=null,
    @Json(name = "admission_date") val admissionDate: String?=null,
    @Json(name = "hospital_record_number") val hospitalRecordNumber: String?=null,
    @Json(name = "surgeon_name") val surgeonName: String?=null,
    @Json(name = "medical_history") val medicalHistory: String?=null,
    @Json(name = "diagnosis") val diagnosis: String?=null,
    @Json(name = "disease_disorder")  val diseaseDisorder: String?=null,
    @Json(name = "nature_of_surgery") val natureOfSurgery: String?=null,
    @Json(name = "stay_length") val stayLength: Int?=null,
    @Json(name = "estimated_cost") val estimatedCost: String?=null,
    @Json(name = "attending_doctor") val attendingDoctor: String?=null,
    @Json(name = "unique_identification_number") val uniqueIdentificationNumber: String?=null,
    @Json(name = "status") val status: Int?=null,
    @Json(name = "created_at") val createdAt: String?=null,
    @Json(name = "updated_at") val updatedAt: String?=null,
    @Json(name = "processed_by") val processedBy:ProcessedBy?=null,
    @Json(name = "presenting_complaints") val presentingComplaints: String?=null,
    @Json(name = "user")  val user: User?=null,
    @Json(name = "family_member")val familyMember: FamilyMember,
    @Json(name = "attachments") val attachments: List<Attachment>
)

class ProcessedBy(
    val id:Int?=null,
    val email:String?=null,
    val fullName:String?=null

)

 class ListPriorAuthorizationsRequest {
     @Json(name = "type")
     var type: String? = null
     @Json(name = "family_member_id")
     var familyMemberId: String? = null
     @Json(name = "prior_auth_id")
     var priorAuthId: Int? = null
     @Json(name = "status")
     val status: String? = null
 }
