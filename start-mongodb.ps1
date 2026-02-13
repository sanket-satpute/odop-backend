$mongoDbPath = "C:\Program Files\MongoDB\Server\7.0\bin\mongod.exe"
$dbPath = "C:\data\db"

# Create data directory if it doesn't exist
if (-not (Test-Path $dbPath)) {
    New-Item -ItemType Directory -Path $dbPath -Force
}

# Start MongoDB
& $mongoDbPath --dbpath $dbPath