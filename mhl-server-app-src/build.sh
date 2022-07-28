
cd mhl-ingest
mvn package
cp target/mhl-ingest.jar ../../mhl-server/mhl-services/ingest
cd ..

cd mhl-vs
mvn package
cp target/mhl-vs.jar ../../mhl-server/mhl-services/vs
cd ..

cd mhl-estap
mvn package
cp target/mhl-estap.jar ../../mhl-server/mhl-services/es-tap
cd ..

cp -R mhl-web ../mhl-server/mhl-services/web