package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity

data class SaleWithItems(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>
)
