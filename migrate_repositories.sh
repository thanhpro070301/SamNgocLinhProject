#!/bin/bash

# Danh sách các repository cần di chuyển
repositories=(
  "ProductRepository.java"
  "OTPRepository.java"
  "OrderItemRepository.java"
  "OrderRepository.java"
  "UserSessionRepository.java"
  "ServiceRepository.java"
  "CategoryRepository.java"
  "NewsRepository.java"
  "ContactRepository.java"
  "ReviewRepository.java"
)

# Thư mục nguồn và đích
SRC_DIR="src/main/java/com/thanhpro0703/SamNgocLinhPJ/reponsitory"
DEST_DIR="src/main/java/com/thanhpro0703/SamNgocLinhPJ/repository"

# Tạo thư mục đích nếu chưa tồn tại
mkdir -p "$DEST_DIR"

# Di chuyển và cập nhật package name cho từng repository
for repo in "${repositories[@]}"; do
  if [ -f "$SRC_DIR/$repo" ]; then
    echo "Processing $repo..."
    # Thay đổi package và di chuyển file
    sed 's/package com.thanhpro0703.SamNgocLinhPJ.reponsitory;/package com.thanhpro0703.SamNgocLinhPJ.repository;/' "$SRC_DIR/$repo" > "$DEST_DIR/$repo"
    echo "Moved $repo to new location"
  else
    echo "Warning: $repo not found in $SRC_DIR"
  fi
done

echo "Migration completed!" 