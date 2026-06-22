package com.example.cursotestingandroid.productlist.data.mappers

import com.example.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity
import com.example.cursotestingandroid.productlist.data.remote.response.PromotionResponse
import com.example.cursotestingandroid.productlist.domain.model.Promotion
import com.example.cursotestingandroid.productlist.domain.model.PromotionType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Instant

fun PromotionResponse.toEntity(json: Json): PromotionEntity? {

    if(startAtEpoch == null || endAtEpoch == null) return null

    val productIds: List<String> = listOf(productId)
    val productIdsJson = json.encodeToString(
        serializer = ListSerializer(String.serializer()),
        value = productIds
    )

    return PromotionEntity(
        id = id,
        productIds = productIdsJson,
        type = type,
        percent = percent,
        buyX = buyX,
        payY = payY,
        startAtEpoch = startAtEpoch,
        endAtEpoch = endAtEpoch
    )
}

fun PromotionEntity.toDomainModel(json: Json): Promotion? {

    val decodedProductIds: List<String>? = runCatching {
        json.decodeFromString(
            deserializer = ListSerializer(String.serializer()),
            string = productIds
        )
    }.getOrNull()

    val finalType = runCatching { PromotionType.valueOf(type.trim().uppercase()) }.getOrNull()

    if(finalType == null || decodedProductIds == null) return null

    val finalOfferValue = when(finalType){
        PromotionType.PERCENT -> percent
        PromotionType.BUY_X_PAY_Y -> payY
    }?.toDouble()

    finalOfferValue ?: return null

    return Promotion(
        id = id,
        type = finalType,
        productIds = decodedProductIds,
        value = finalOfferValue,
        buyQuantity = buyX,
        startTime = Instant.ofEpochSecond(startAtEpoch),
        endTime = Instant.ofEpochSecond(endAtEpoch)
    )
}
