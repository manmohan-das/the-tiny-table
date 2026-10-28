-- MySQL dump 10.13  Distrib 26.7.0, for Win64 (x86_64)
--
-- Host: localhost    Database: the_tiny_table
-- ------------------------------------------------------
-- Server version	26.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '8384c127-f377-11f0-b6c1-a841f494b21c:1-2328,
acceae0f-ae63-11f1-b912-cc4740d32571:1-279';

--
-- Table structure for table `app_settings`
--

DROP TABLE IF EXISTS `app_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_settings` (
  `setting_id` int NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) NOT NULL,
  `setting_value` text NOT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `setting_key` (`setting_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_settings`
--

LOCK TABLES `app_settings` WRITE;
/*!40000 ALTER TABLE `app_settings` DISABLE KEYS */;
INSERT INTO `app_settings` VALUES (1,'gst_rate','5.00','2026-10-26 11:58:49'),(2,'upi_id','8002870395@ybl','2026-10-26 11:58:49'),(3,'upi_qr','qr_image','2026-10-26 12:19:10');
/*!40000 ALTER TABLE `app_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (2,'Chinese'),(6,'Dessert'),(5,'Drinks'),(1,'Fast Food'),(7,'Indian dish'),(3,'Pizza'),(4,'Snacks');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `food_items`
--

DROP TABLE IF EXISTS `food_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `food_items` (
  `food_id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `food_name` varchar(150) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `available` tinyint(1) NOT NULL DEFAULT '1',
  `available_qty` int NOT NULL DEFAULT '0',
  `food_type` enum('Veg','Non-Veg') NOT NULL DEFAULT 'Veg',
  PRIMARY KEY (`food_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `food_items_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `food_items`
--

LOCK TABLES `food_items` WRITE;
/*!40000 ALTER TABLE `food_items` DISABLE KEYS */;
INSERT INTO `food_items` VALUES (1,1,'Burger',80.00,'resources/foodImages/burger.png',1,20,'Veg'),(2,1,'Sandwich',60.00,'resources/foodImages/sandwich.png',1,15,'Veg'),(3,2,'Veg chowmin',70.00,'images/1793172623028.png',1,22,'Veg'),(4,2,'Manchurian',90.00,'resources/foodImages/manchurian.png',1,15,'Veg'),(5,3,'Pizza',150.00,'resources/foodImages/pizza.png',1,10,'Veg'),(6,1,'Veg Momos',60.00,'images/1793171738222.png',1,80,'Veg'),(7,4,'Samosa',20.00,'resources/foodImages/samosa.png',0,0,'Veg'),(8,1,'Egg Roll',60.00,'images/1793172694983.png',0,0,'Non-Veg'),(9,4,'French Fries',80.00,'resources/foodImages/french fries.png',1,20,'Veg'),(10,5,'Cold Drink',40.00,'resources/foodImages/cold drink.png',1,30,'Veg'),(11,5,'Cold Coffee',70.00,'resources/foodImages/cold coffee.png',1,20,'Veg'),(14,3,'Margherita',189.00,'images/1793169686201.png',1,16,'Veg'),(15,3,'Chicken pizza',249.00,'images/1793169754247.png',1,18,'Non-Veg'),(16,5,'Bluberry smoothie',219.00,'images/1793169799129.png',1,23,'Veg'),(17,5,'chocolate shake',199.00,'images/1793169849982.png',0,0,'Veg'),(18,5,'Lemon water',129.00,'images/1793172986439.png',0,0,'Veg'),(19,1,'Chicken momo',80.00,'images/1793170031367.png',1,23,'Non-Veg'),(20,4,'Chicken pakora',99.00,'images/1793172659917.png',1,90,'Non-Veg'),(21,2,'Egg chowmein',149.00,'images/1793170162023.png',1,22,'Non-Veg'),(22,3,'Veggie pizza',289.00,'images/1793170213757.png',1,18,'Veg'),(24,2,'White sauce pasta',249.00,'images/1793170335890.png',0,0,'Veg'),(25,6,'Vanilla cake',400.00,'images/1793170398514.png',1,20,'Veg'),(26,4,'Bread pakora',70.00,'images/1793170442110.png',1,46,'Veg'),(27,6,'Chocolate brownie',299.00,'images/1793170708590.png',1,18,'Veg'),(28,6,'Muffins',160.00,'images/1793170776692.png',1,22,'Veg'),(29,5,'Strawberry smoothie',180.00,'images/1793171008971.png',1,22,'Veg'),(30,6,'Gulabjamun',20.00,'images/1793171097622.png',1,80,'Veg'),(31,7,'Chhole bhature',80.00,'images/1793171207910.png',1,60,'Veg'),(32,7,'Poha',80.00,'images/1793172558523.png',1,55,'Veg'),(33,7,'Paneer paratha',90.00,'images/1793171562893.png',1,33,'Veg'),(34,6,'Tiramisu',220.00,'images/1793171631047.png',1,26,'Veg');
/*!40000 ALTER TABLE `food_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gst_settings`
--

DROP TABLE IF EXISTS `gst_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gst_settings` (
  `setting_key` varchar(100) NOT NULL,
  `setting_value` decimal(7,3) NOT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gst_settings`
--

LOCK TABLES `gst_settings` WRITE;
/*!40000 ALTER TABLE `gst_settings` DISABLE KEYS */;
INSERT INTO `gst_settings` VALUES ('gst_rate_percent',10.000,'2026-10-24 13:06:08');
/*!40000 ALTER TABLE `gst_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `food_id` int NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `order_id` (`order_id`),
  KEY `food_id` (`food_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`food_id`) REFERENCES `food_items` (`food_id`)
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,1,1,80.00,80.00),(2,1,6,1,80.00,80.00),(3,1,7,2,20.00,40.00),(4,2,5,1,150.00,150.00),(5,2,9,1,80.00,80.00),(6,3,3,1,70.00,70.00),(7,3,8,1,60.00,60.00),(8,3,7,1,20.00,20.00),(9,4,5,2,150.00,300.00),(10,5,1,1,80.00,80.00),(11,5,10,1,40.00,40.00),(12,5,11,1,70.00,70.00),(15,6,10,1,40.00,40.00),(16,7,10,1,40.00,40.00),(17,7,11,1,70.00,70.00),(18,8,4,1,90.00,90.00),(19,9,10,1,40.00,40.00),(20,10,4,1,90.00,90.00),(21,11,4,1,90.00,90.00),(22,12,10,1,40.00,40.00),(27,13,3,1,70.00,70.00),(28,13,10,1,40.00,40.00),(29,13,2,1,60.00,60.00),(30,13,8,5,60.00,300.00),(31,13,4,1,90.00,90.00),(32,14,10,4,40.00,160.00),(33,14,2,4,60.00,240.00),(34,14,1,4,80.00,320.00),(35,14,11,4,70.00,280.00),(36,15,10,1,40.00,40.00),(37,16,4,2,90.00,180.00),(65,18,4,1,90.00,90.00),(66,18,10,1,40.00,40.00),(67,18,3,1,70.00,70.00),(68,18,11,1,70.00,70.00),(69,18,1,1,80.00,80.00),(70,18,2,1,60.00,60.00),(71,18,8,1,60.00,60.00),(72,18,6,1,80.00,80.00),(73,17,4,2,90.00,180.00),(74,17,11,1,70.00,70.00),(75,17,1,1,80.00,80.00),(76,17,10,1,40.00,40.00),(80,20,10,1,40.00,40.00),(81,20,2,1,60.00,60.00),(82,20,8,1,60.00,60.00),(83,20,6,1,80.00,80.00),(86,21,10,1,40.00,40.00),(87,21,2,1,60.00,60.00),(88,19,3,1,70.00,70.00),(89,19,10,2,40.00,80.00),(90,22,10,1,40.00,40.00),(91,23,10,1,40.00,40.00),(92,24,10,1,40.00,40.00),(93,25,4,1,90.00,90.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `customer_name` varchar(100) NOT NULL,
  `customer_phone` varchar(15) DEFAULT NULL,
  `order_date` datetime NOT NULL,
  `queue_no` int NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `discount` decimal(10,2) DEFAULT '0.00',
  `tax` decimal(10,2) DEFAULT '0.00',
  `total_amount` decimal(10,2) NOT NULL,
  `status` enum('Pending','Preparing','Ready','Completed','Cancelled') NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `employee_id` (`employee_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,2,'Rahul Kumar','9876543210','2026-09-26 14:55:58',1,200.00,0.00,10.00,210.00,'Completed',NULL),(2,2,'Aman Kumar','9876543211','2026-09-26 14:55:58',2,230.00,10.00,11.00,231.00,'Completed',NULL),(3,3,'Rohit Singh','9876543212','2026-09-26 14:55:58',3,150.00,0.00,7.50,157.50,'Completed',NULL),(4,3,'Vikas Kumar','9876543213','2026-09-26 14:55:58',4,300.00,20.00,14.00,294.00,'Completed',NULL),(5,2,'Ankit Raj','9876543214','2026-09-26 14:55:58',5,170.00,0.00,8.50,178.50,'Completed',NULL),(6,3,'Walk-in Guest',NULL,'2026-10-04 09:29:33',1,40.00,0.00,4.80,44.80,'Cancelled',''),(7,3,'raman',NULL,'2026-10-04 09:57:08',2,110.00,13.20,11.62,108.42,'Completed',''),(8,3,'Walk-in Guest',NULL,'2026-10-04 16:18:59',3,0.00,0.00,0.00,0.00,'Preparing',''),(9,3,'Walk-in Guest',NULL,'2026-10-04 17:50:17',4,40.00,0.00,4.80,44.80,'Completed',''),(10,3,'Walk-in Guest',NULL,'2026-10-04 19:53:45',5,90.00,0.00,10.80,100.80,'Cancelled',''),(11,3,'Walk-in Guest',NULL,'2026-10-04 20:38:19',6,90.00,0.00,10.80,100.80,'Preparing',''),(12,3,'Walk-in Guest',NULL,'2026-10-04 21:01:40',7,40.00,0.00,4.80,44.80,'Pending',''),(13,3,'manmohan',NULL,'2026-10-05 11:23:27',1,560.00,28.00,63.84,595.84,'Pending',''),(14,2,'Walk-in Guest',NULL,'2026-10-06 16:09:49',1,1000.00,0.00,120.00,1120.00,'Pending',''),(15,2,'Walk-in Guest',NULL,'2026-10-06 16:19:22',2,40.00,0.00,4.80,44.80,'Pending',''),(16,2,'Manmohan',NULL,'2026-10-06 17:14:31',3,180.00,0.00,21.60,201.60,'Pending',''),(17,2,'Walk-in Guest',NULL,'2026-10-24 12:38:00',1,370.00,0.00,44.40,414.40,'Cancelled',''),(18,2,'Walk-in Guest',NULL,'2026-10-24 16:07:44',2,550.00,0.00,66.00,616.00,'Completed',''),(19,2,'Walk-in Guest',NULL,'2026-10-24 16:19:06',3,150.00,0.00,15.20,165.20,'Pending',''),(20,2,'Walk-in Guest',NULL,'2026-10-24 17:07:55',4,240.00,0.00,28.80,268.80,'Pending',''),(21,2,'Walk-in Guest',NULL,'2026-10-24 18:16:57',5,100.00,0.00,5.00,105.00,'Pending',''),(22,2,'Walk-in Guest',NULL,'2026-10-24 20:37:25',6,40.00,0.00,4.00,44.00,'Pending',''),(23,2,'Walk-in Guest',NULL,'2026-10-24 21:23:58',7,40.00,0.00,4.00,44.00,'Pending',''),(24,2,'manmohan','1029384756','2026-10-26 20:06:33',1,40.00,0.00,2.00,42.00,'Pending',''),(25,2,'manmohan','1234567890','2026-10-26 21:17:20',2,90.00,0.00,4.50,94.50,'Pending','');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('Cash','UPI','Card') DEFAULT NULL,
  `payment_status` enum('Pending','Paid','Failed','Refunded') NOT NULL,
  `payment_date` datetime DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `order_id` (`order_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,1,210.00,'Cash','Paid',NULL),(2,2,231.00,'UPI','Paid','2026-09-26 14:55:58'),(3,3,157.50,'Cash','Paid','2026-09-26 14:55:58'),(4,4,294.00,'Card','Paid','2026-09-26 14:55:58'),(5,5,178.50,'UPI','Paid','2026-09-26 14:55:58'),(9,6,44.80,'Cash','Paid','2026-10-04 09:29:33'),(10,7,39.42,'Cash','Paid','2026-10-04 09:57:08'),(11,8,100.80,'Cash','Paid','2026-10-04 16:18:59'),(12,9,44.80,'Cash','Paid','2026-10-04 17:50:17'),(13,10,100.80,'Cash','Paid','2026-10-04 19:53:45'),(14,11,100.80,'Cash','Paid','2026-10-04 20:38:19'),(15,12,44.80,'Cash','Paid','2026-10-04 21:01:40'),(16,13,500.08,'Cash','Paid','2026-10-05 11:23:27'),(17,14,1120.00,'Cash','Paid','2026-10-06 16:09:49'),(18,15,44.80,'Cash','Paid','2026-10-06 16:19:22'),(19,16,201.60,'Cash','Paid','2026-10-06 17:14:31'),(20,17,414.40,'Card','Paid','2026-10-24 16:08:01'),(24,18,616.00,'Cash','Paid','2026-10-24 16:07:44'),(25,19,165.20,'UPI','Paid','2026-10-24 18:19:43'),(26,20,268.80,'Cash','Paid','2026-10-24 17:07:55'),(27,21,105.00,'Cash','Paid','2026-10-24 18:16:57'),(28,22,44.00,'UPI','Paid','2026-10-24 20:37:25'),(29,23,44.00,'Card','Paid','2026-10-24 21:23:58'),(30,24,42.00,'UPI','Paid','2026-10-26 20:06:33'),(31,25,94.50,'UPI','Paid','2026-10-26 21:17:20');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_profile`
--

DROP TABLE IF EXISTS `store_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_profile` (
  `profile_id` tinyint unsigned NOT NULL,
  `store_name` varchar(150) NOT NULL DEFAULT 'The Tiny Table',
  `address` varchar(255) NOT NULL DEFAULT '',
  `phone` varchar(30) NOT NULL DEFAULT '',
  `gstin` varchar(30) NOT NULL DEFAULT '',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`profile_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_profile`
--

LOCK TABLES `store_profile` WRITE;
/*!40000 ALTER TABLE `store_profile` DISABLE KEYS */;
INSERT INTO `store_profile` VALUES (1,'The Tiny Table','Purnea','8002870395','GSTIN00001','2026-10-24 16:09:33');
/*!40000 ALTER TABLE `store_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('manager','employee','kitchen_staff') NOT NULL DEFAULT 'employee',
  `salary` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Raman','r@man12','admin123','manager',100000.00,1),(2,'Employee1','employee1','123456','employee',70000.00,1),(3,'Amit Kumar','amit','123456','employee',30000.00,1),(4,'Kitchen Staff 1','kitchen1','123456','kitchen_staff',25000.00,1),(5,'Kitchen Staff 2','kitchen2','123456','kitchen_staff',20000.00,1),(9,'Kumkum kumari','kumkum','ku1234','manager',100000.00,1),(11,'Raman','raman','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','employee',134543.00,1),(14,'Manmohan','manmohan','123456','manager',100000.00,1),(15,'Milan','milan','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','manager',100000.00,1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-28 13:14:38
