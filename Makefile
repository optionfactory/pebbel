
build:
	mvn clean build
deploy-oss:
	mvn -P ossrh clean deploy
