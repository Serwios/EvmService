# EvmService
![image](https://github.com/user-attachments/assets/6ea3e317-68dc-4b6d-9107-b9894717f326)

This project implements a database structure for managing blockchain transactions, utilizing partitioning and advanced full-text search capabilities using PostgreSQL. The system stores transaction data, with efficient indexing and partitioning for optimal performance in large datasets. Additionally, it supports full-text search and partial word matching using trigram indexing.

# Features
* Partitioned Transactions Table: The transactions table is partitioned by block number to improve query performance and manage large data volumes.
* Full-Text Search: A custom function and trigger are used to update the search_vector column, which is indexed using PostgreSQL's GIN index for full-text search.
* Partial Word Matching: Trigram indexing (pg_trgm) is used to support partial word matching on from_address and to_address columns.
* Blockchain Transaction Data: The system tracks essential blockchain transaction fields such as hash, from_address, to_address, value, gas, gas_price, and more.

# Prerequisites
1. Create account here and acquire api key -  https://app.infura.io/
2. Set key in this file : .env
INFURA_API_KEY=
2. Install docker and run docker compose up

# Setup Instructions
1. Clone this repo: git clone https://github.com/Serwios/EvmService.git
2. cd EvmService
3. docker-compose up --build

# Useful links
| Name | Link | 
|----------|----------|
| Swagger | http://localhost:8080/swagger-ui.html | 
| Api Docs | http://localhost:8080/v3/api-docs | 
| Health | http://localhost:8080/actuator/health | 
| Metrics | http://localhost:8080/actuator/metrics | 

# Postgres configuration
**Partitioning**
The transactions table is partitioned by the block_number field. Each partition stores transactions for a specific range of blocks. This improves query performance by limiting the number of rows that need to be scanned when querying transactions in a specific block range.

**Indexes**
The following indexes are created for optimal performance:
Primary Key Index: The hash and block_number columns are indexed to enforce uniqueness for each transaction.
Search Vector Index: A GIN index is created on the search_vector column to support efficient full-text search.
Trigram Indexes: Trigram-based indexes (pg_trgm) are created for the from_address and to_address columns, enabling partial word matching.
Triggers
A trigger (tsvectorupdate) is set up to automatically update the search_vector column whenever a transaction is inserted or updated.

**Partition Creation**
New partitions are created automatically based on the range of block numbers, ensuring efficient storage and query performance as new transactions are added.\
