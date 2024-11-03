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
	    
        DOCKER_REPOSITORY_NAME = 'rimhammami_g2_gestionstationski'
	    DOCKER_REPOSITORY_NAMESPACE = 'hammamirim'
	    DOCKER_REPOSITORY = "${DOCKER_REPOSITORY_NAMESPACE}/${DOCKER_REPOSITORY_NAME}:${VERSION}"
	    DOCKER_REPOSITORY_LATEST = "${DOCKER_REPOSITORY_NAMESPACE}/${DOCKER_REPOSITORY_NAME}:latest"

	    APP_IMAGE = "${DOCKER_REPOSITORY_NAME}:${VERSION}"
    }

    stages {
        stage('Clean dependencies') {
            steps {
                script {
                    sh 'mvn clean'
                }
            }
        }
 
        stage('Version update') {
            steps {
                script {
		    sh "mvn versions:set -DnewVersion=${VERSION}"
                }
            }
        }

        stage('Compile source code') {
            steps {
                script {
                    sh 'mvn compile -DskipTests'
                }
            }
        }


        stage('Unit tests') {
            steps {
                script {
                    sh 'mvn verify test -DskipCompile'
                }
            }
        }

        stage('Sonar check') {
            steps {
		script {
                    sh 'mvn sonar:sonar -Dsonar.host.url=http://sonar:9000'
                }
            }
        }

        stage('Build jar') {
            steps {
                script {
                    sh 'mvn package -DskipTests -DskipCompile'
                }
            }
        }

        stage('Deploy on Nexus') {
            steps {
                script {
		            sh "mvn deploy -DskipTests -DskipCompile -DskipPackaging -s mvn-settings.xml -P snapshot"
                }
            }
        }

        stage('Build docker image') {
            steps {
                script {
                    sh "docker build -t ${APP_IMAGE} ."
                }
            }
        }
        stage('Push Docker Image to Docker hub') {
            steps {
                script {
		    sh '''
                    echo "$DOCKERHUB_PASSWORD" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
		    docker tag "$APP_IMAGE" "$DOCKER_REPOSITORY"
		    docker tag "$APP_IMAGE" "$DOCKER_REPOSITORY_LATEST"
		    docker push "$DOCKER_REPOSITORY"
		    docker push "$DOCKER_REPOSITORY_LATEST"
                    '''
                }
            }
        }
        stage('Stop and drop Containers') {
            steps {
                script {
                    sh 'docker-compose down'
                }
            }
        }
        stage('Remove old image') {
            steps {
                script {
                    try {
                        sh 'docker image rm "$DOCKER_REPOSITORY" -f && docker image rm "$DOCKER_REPOSITORY_LATEST" -f'
                    } catch (Exception e) {
                        echo "An error occurred while removing old images: ${e.message}"
                    }
                }
                
            }
        }
        stage('Pulling images and restart Container') {
            steps {
                script {
                    sh 'docker-compose up -d'
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
