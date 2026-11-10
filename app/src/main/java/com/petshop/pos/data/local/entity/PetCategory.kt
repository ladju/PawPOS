package com.petshop.pos.data.local.entity

enum class PetCategory(val displayName: String, val icon: String) {
    ALL("All Products", "🐾"),
    DOGS("Dogs", "🐶"),
    CATS("Cats", "🐱"),
    BIRDS("Birds", "🦜"),
    AQUATICS("Aquatics", "🐠"),
    SMALL_PETS("Small Pets", "🐹"),
    SERVICES("Services", "✂️");

    companion object {
        fun fromString(name: String): PetCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: ALL
        }
    }
}

enum class ProductType(val displayName: String) {
    FOOD("Food & Treats"),
    HEALTH("Health & Care"),
    TOY("Toys & Play"),
    ACCESSORY("Accessories & Housing"),
    GROOMING("Grooming & Hygiene"),
    SERVICE("Service")
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CARD("Debit/Credit Card"),
    QRIS("QRIS / E-Wallet"),
    TRANSFER("Bank Transfer")
}

enum class StockChangeReason(val displayName: String) {
    RESTOCK("Restock / Shipment Arrival"),
    SALE("Customer Sale"),
    DAMAGED("Damaged / Defect"),
    EXPIRED("Expired Goods Disposal"),
    AUDIT("Inventory Count Adjustment")
}
