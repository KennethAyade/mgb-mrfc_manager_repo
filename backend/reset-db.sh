#!/bin/bash

echo "================================================"
echo "DATABASE RESET - MGB MRFC MANAGER"
echo "================================================"
echo ""
echo "WARNING: This will DELETE ALL DATA in the database!"
echo "Only the superadmin user will remain."
echo ""
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" = "yes" ]; then
    echo ""
    echo "Starting database reset..."
    cd backend
    npm run db:reset
else
    echo ""
    echo "Database reset cancelled."
fi



