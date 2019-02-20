build:
	mvn clean package
deploy-oss:
	mvn -P ossrh clean deploy
