pipeline {
    agent any

    tools {
        maven 'Maven 3.9.9'
    }

    environment {
	VERSION = "1.0.${BUILD_NUMBER}-SNAPSHOT"

        SONAR_TOKEN = credentials('SONAR_TOKEN')

        NEXUS_USERNAME = credentials('NEXUS_USERNAME')
        NEXUS_PASSWORD = credentials('NEXUS_PASSWORD')

        DOCKERHUB_USERNAME = credentials('DOCKERHUB_USERNAME')
        DOCKERHUB_PASSWORD = credentials('DOCKERHUB_PASSWORD')
	DOCKER_REPOSITORY_NAME = 'belhassanromdhani_g2_gestionstationski'
	DOCKER_REPOSITORY_NAMESPACE = 'belhassan123'
	DOCKER_REPOSITORY = "${DOCKER_REPOSITORY_NAMESPACE}/${DOCKER_REPOSITORY_NAME}:${VERSION}"

	APP_IMAGE = "${DOCKER_REPOSITORY_NAME}:${VERSION}"
    }

    stages {
        stage('Clean') {
            steps {
                script {
                    sh 'mvn clean'
                }
            }
        }

        stage('Version') {
            steps {
                script {
		    sh "mvn versions:set -DnewVersion=${VERSION}"
                }
            }
        }

        stage('Compiling') {
            steps {
                script {
                    sh 'mvn compile -DskipTests'
                }
            }
        }


        stage('Junit & Mockito test dynamique ') {
            steps {
                script {
                    sh 'mvn verify test -DskipCompile'
                }
            }
        }

        stage('Sonar-Test - statique') {
            steps {
		script {
                    sh 'mvn sonar:sonar -Dsonar.host.url=http://sonar:9000'
                }
            }
        }

        stage('Package-JAR') {
            steps {
                script {
                    sh 'mvn package -DskipTests -DskipCompile'
                }
            }
        }

        stage('Nexus-Deploy') {
            steps {
                script {
		    sh "mvn deploy -DskipTests -DskipCompile -DskipPackaging -s mvn-settings.xml -P snapshot"
                }
            }
        }

        stage('Build-Image') {
            steps {
                script {
                    sh "docker build -t ${APP_IMAGE} ."
                }
            }
        }
        stage('IMAGE-Push-Dockerhub') {
            steps {
                script {
		    sh '''
                    echo "$DOCKERHUB_PASSWORD" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
		    docker tag "$APP_IMAGE" "$DOCKER_REPOSITORY"
		    docker push "$DOCKER_REPOSITORY"
		    docker image rm "$APP_IMAGE"
                    '''
                }
            }
        }
        stage('Container Deployment') {
            steps {
                script {
                    sh 'docker-compose down && docker-compose up -d --build'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed!'
        }
        success {
            echo 'Build was successful!'
        }
        failure {
            echo 'Build failed.'
        }
    }
}
