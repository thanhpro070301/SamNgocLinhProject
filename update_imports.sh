#!/bin/bash

# Tìm tất cả các file Java trong thư mục src
find src -name "*.java" -type f | while read file; do
  # Thay thế tất cả các import của package reponsitory
  sed -i 's/import com\.thanhpro0703\.SamNgocLinhPJ\.reponsitory\./import com.thanhpro0703.SamNgocLinhPJ.repository./g' "$file"
  echo "Updated $file"
done

echo "Import updates completed!" 