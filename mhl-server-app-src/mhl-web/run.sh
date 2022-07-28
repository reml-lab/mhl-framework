FILE=/data/db.sqlite3
if [ -f "$FILE" ]; then
    echo "$FILE exists."
else 
    echo "$FILE does not exist. Creating."
    mv /mhl-web/db.sqlite3 /data/db.sqlite3
fi

python3 manage.py runserver 0.0.0.0:8000