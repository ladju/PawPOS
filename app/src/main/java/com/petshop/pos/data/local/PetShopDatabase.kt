package com.petshop.pos.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.petshop.pos.data.local.dao.ProductDao
import com.petshop.pos.data.local.dao.StockMovementDao
import com.petshop.pos.data.local.dao.TransactionDao
import com.petshop.pos.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Converters {
    @TypeConverter
    fun fromPetCategory(value: PetCategory): String = value.name

    @TypeConverter
    fun toPetCategory(value: String): PetCategory = PetCategory.fromString(value)

    @TypeConverter
    fun fromProductType(value: ProductType): String = value.name

    @TypeConverter
    fun toProductType(value: String): ProductType = try {
        ProductType.valueOf(value)
    } catch (e: Exception) {
        ProductType.FOOD
    }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try {
        PaymentMethod.valueOf(value)
    } catch (e: Exception) {
        PaymentMethod.CASH
    }

    @TypeConverter
    fun fromStockChangeReason(value: StockChangeReason): String = value.name

    @TypeConverter
    fun toStockChangeReason(value: String): StockChangeReason = try {
        StockChangeReason.valueOf(value)
    } catch (e: Exception) {
        StockChangeReason.RESTOCK
    }
}

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetShopDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: PetShopDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PetShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetShopDatabase::class.java,
                    "petshop_database.db"
                )
                    .addCallback(PetShopDatabaseCallback(scope) { INSTANCE!! })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PetShopDatabaseCallback(
        private val scope: CoroutineScope,
        private val databaseProvider: () -> PetShopDatabase
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                populateInitialData(databaseProvider().productDao())
            }
        }

        private suspend fun populateInitialData(productDao: ProductDao) {
            val initialProducts = listOf(
                ProductEntity(
                    barcode = "8991001",
                    name = "Royal Canin Mini Adult Dog Food 2kg",
                    category = PetCategory.DOGS,
                    productType = ProductType.FOOD,
                    costPrice = 165000.0,
                    sellingPrice = 245000.0,
                    currentStock = 18,
                    minStockAlert = 5,
                    unit = "bag",
                    expiryDate = "2027-08-10",
                    description = "Complete feed for adult small breed dogs (from 1 to 10 kg) - Over 10 months old."
                ),
                ProductEntity(
                    barcode = "8991002",
                    name = "Pedigree Dentastix Medium Dog Treats",
                    category = PetCategory.DOGS,
                    productType = ProductType.FOOD,
                    costPrice = 22000.0,
                    sellingPrice = 35000.0,
                    currentStock = 4,
                    minStockAlert = 8,
                    unit = "pack",
                    expiryDate = "2027-11-20",
                    description = "Daily oral care chews scientifically proven to reduce tartar buildup."
                ),
                ProductEntity(
                    barcode = "8991003",
                    name = "Frontline Plus Flea & Tick Treatment",
                    category = PetCategory.DOGS,
                    productType = ProductType.HEALTH,
                    costPrice = 120000.0,
                    sellingPrice = 185000.0,
                    currentStock = 12,
                    minStockAlert = 4,
                    unit = "box",
                    expiryDate = "2028-01-15",
                    description = "Fast-acting, long-lasting flea and tick protection for dogs 10-20kg."
                ),
                ProductEntity(
                    barcode = "8991004",
                    name = "KONG Classic Durable Rubber Chew Toy",
                    category = PetCategory.DOGS,
                    productType = ProductType.TOY,
                    costPrice = 65000.0,
                    sellingPrice = 110000.0,
                    currentStock = 15,
                    minStockAlert = 3,
                    unit = "pcs",
                    description = "Ultra-durable natural rubber toy that fulfills instinctual chewing needs."
                ),
                ProductEntity(
                    barcode = "8992001",
                    name = "Whiskas Ocean Fish Adult Dry Cat Food 1.2kg",
                    category = PetCategory.CATS,
                    productType = ProductType.FOOD,
                    costPrice = 52000.0,
                    sellingPrice = 78000.0,
                    currentStock = 22,
                    minStockAlert = 6,
                    unit = "bag",
                    expiryDate = "2027-09-01",
                    description = "100% complete and balanced dry cat food with real ocean fish."
                ),
                ProductEntity(
                    barcode = "8992002",
                    name = "Sheba Premium Wet Cat Food Tuna & Salmon",
                    category = PetCategory.CATS,
                    productType = ProductType.FOOD,
                    costPrice = 8500.0,
                    sellingPrice = 14000.0,
                    currentStock = 45,
                    minStockAlert = 10,
                    unit = "pouch",
                    expiryDate = "2027-12-15",
                    description = "Luxurious tender flakes in culinary gravy."
                ),
                ProductEntity(
                    barcode = "8992003",
                    name = "Tidy Cats Clumping Bentonite Litter 10kg",
                    category = PetCategory.CATS,
                    productType = ProductType.GROOMING,
                    costPrice = 60000.0,
                    sellingPrice = 95000.0,
                    currentStock = 2,
                    minStockAlert = 5,
                    unit = "bag",
                    description = "Superior odor control with quick clumping natural bentonite clay."
                ),
                ProductEntity(
                    barcode = "8992004",
                    name = "Cat Feather Wand & Bell Interactive Toy",
                    category = PetCategory.CATS,
                    productType = ProductType.TOY,
                    costPrice = 12000.0,
                    sellingPrice = 25000.0,
                    currentStock = 30,
                    minStockAlert = 5,
                    unit = "pcs",
                    description = "Flexible wand with natural feathers and jingle bell."
                ),
                ProductEntity(
                    barcode = "8993001",
                    name = "Kaytee Forti-Diet Pro Health Canary & Finch",
                    category = PetCategory.BIRDS,
                    productType = ProductType.FOOD,
                    costPrice = 38000.0,
                    sellingPrice = 62000.0,
                    currentStock = 14,
                    minStockAlert = 4,
                    unit = "bag",
                    expiryDate = "2027-10-30",
                    description = "Enriched seed blend with DHA Omega-3 and probiotics for vibrant feathers."
                ),
                ProductEntity(
                    barcode = "8993002",
                    name = "Prevue Hanging Bird Cage with Feeders",
                    category = PetCategory.BIRDS,
                    productType = ProductType.ACCESSORY,
                    costPrice = 210000.0,
                    sellingPrice = 350000.0,
                    currentStock = 3,
                    minStockAlert = 2,
                    unit = "unit",
                    description = "Powder-coated bird cage with perches and easy-clean removable tray."
                ),
                ProductEntity(
                    barcode = "8994001",
                    name = "TetraMin Tropical Flakes Fish Food 100g",
                    category = PetCategory.AQUATICS,
                    productType = ProductType.FOOD,
                    costPrice = 32000.0,
                    sellingPrice = 55000.0,
                    currentStock = 25,
                    minStockAlert = 5,
                    unit = "can",
                    expiryDate = "2028-03-01",
                    description = "Nutritionally balanced staple diet for all tropical aquarium fish."
                ),
                ProductEntity(
                    barcode = "8994002",
                    name = "Sunsun Submersible Aquarium Filter 500L/H",
                    category = PetCategory.AQUATICS,
                    productType = ProductType.ACCESSORY,
                    costPrice = 65000.0,
                    sellingPrice = 115000.0,
                    currentStock = 8,
                    minStockAlert = 3,
                    unit = "unit",
                    description = "Quiet multi-stage filtration for fresh and saltwater aquariums."
                ),
                ProductEntity(
                    barcode = "8995001",
                    name = "Oxbow Essentials Hamster & Gerbil 450g",
                    category = PetCategory.SMALL_PETS,
                    productType = ProductType.FOOD,
                    costPrice = 48000.0,
                    sellingPrice = 75000.0,
                    currentStock = 1,
                    minStockAlert = 4,
                    unit = "bag",
                    expiryDate = "2027-06-20",
                    description = "Fortified diet supporting healthy teeth and digestive wellness."
                ),
                ProductEntity(
                    barcode = "8995002",
                    name = "Carefresh Natural Paper Bedding 10L",
                    category = PetCategory.SMALL_PETS,
                    productType = ProductType.ACCESSORY,
                    costPrice = 45000.0,
                    sellingPrice = 72000.0,
                    currentStock = 11,
                    minStockAlert = 3,
                    unit = "pack",
                    description = "Soft, 99% dust-free ultra-absorbent paper bedding for small pets."
                ),
                ProductEntity(
                    barcode = "8996001",
                    name = "Full Pet Spa & Grooming (Bath & Cut)",
                    category = PetCategory.SERVICES,
                    productType = ProductType.SERVICE,
                    costPrice = 35000.0,
                    sellingPrice = 150000.0,
                    currentStock = 999,
                    minStockAlert = 0,
                    unit = "session",
                    description = "Complete shampoo, blow-dry, nail trim, ear clean, and custom styling."
                ),
                ProductEntity(
                    barcode = "8996002",
                    name = "Nail Trimming & Ear Cleaning Service",
                    category = PetCategory.SERVICES,
                    productType = ProductType.SERVICE,
                    costPrice = 10000.0,
                    sellingPrice = 45000.0,
                    currentStock = 999,
                    minStockAlert = 0,
                    unit = "session",
                    description = "Quick and gentle nail clip and hygienic ear canal cleaning."
                )
            )
            productDao.insertProducts(initialProducts)
        }
    }
}
