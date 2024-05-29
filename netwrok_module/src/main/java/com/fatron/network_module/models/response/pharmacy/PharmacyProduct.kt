package com.fatron.network_module.models.response.pharmacy

import com.fatron.network_module.models.request.partnerprofile.Media
import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json

data class PharmacyProduct(
    @Json(name = "category")
    val category: GenericItem? = null,
    @Json(name = "category_id")
    val categoryId: Int? = null,
    @Json(name = "code")
    val code: String? = null,
    @Json(name = "currency_id")
    val currencyId: Int? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "display_name")
    var displayName: String? = null,
    @Json(name = "dosage")
    val dosage: GenericItem? = null,
    @Json(name = "dosage_form_id")
    val dosageFormId: Int? = null,
    @Json(name = "generic")
    val generic: Generic? = null,
    @Json(name = "generic_id")
    val genericId: Int? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "manufacturer")
    val manufacturer: GenericItem? = null,
    @Json(name = "manufacturer_id")
    val manufacturerId: Int? = null,
    @Json(name = "media")
    val media: Media? = null,
    @Json(name = "old_price")
    val oldPrice: String? = null,
    @Json(name = "package_size")
    val packageSize: Int? = null,
    @Json(name = "package_type")
    val packageType: GenericItem? = null,
    @Json(name = "package_type_id")
    val packageTypeId: Int? = null,
    @Json(name = "prescription_required")
    val prescriptionRequired: Int? = null,
    @Json(name = "price")
    val price: String? = null,
    @Json(name = "product_image_id")
    val productImageId: Int? = null,
    @Json(name = "selling_unit_id")
    val sellingUnitId: Int? = null,
    @Json(name = "selling_volume")
    val sellingVolume: Int? = null,
    @Json(name = "type_id")
    val typeId: Int? = null,
    @Json(name = "related_items")
    val relatedItems: List<PharmacyProduct>? = null,


    @Json(name = "booking_id")
    val bookingId: Int?,
    @Json(name = "discount")
    val discount: Int?,
    @Json(name = "is_available")
    val isAvailable: Any?,
    @Json(name = "is_substitute")
    val isSubstitute: Any?,
    @Json(name = "product_id")
    val productId: Int?,
    @Json(name = "quantity")
    val quantity: Int?,
    @Json(name = "subtotal")
    val subtotal: Double?,

    @Json(name = "booking_pharmacy_product")
    val booking_pharmacy_product: BookingPharmacyProduct? = null
)

data class BookingPharmacyProduct(
    val quantity: Int? = null
)