pipeline {
    agent any

    tools {
        maven 'Maven 3.9.9'
    }

    environment {
	    VERSION = "1.0.${new Date().format('yyyyMMddHHmmss')}-SNAPSHOT"

        SONAR_TOKEN = credentials('SONAR_TOKEN')

        NEXUS_USERNAME = credentials('NEXUS_USERNAME')
        NEXUS_PASSWORD = credentials('NEXUS_PASSWORD')

        DOCKERHUB_USERNAME = credentials('DOCKERHUB_USERNAME')
        DOCKERHUB_PASSWORD = credentials('DOCKERHUB_PASSWORD')
        
        
        DOCKER_REPOSITORY = "moatez1/moataztej_g2_gestionstationski:${VERSION}"
        DOCKER_REPOSITORY_LATEST = "moatez1/moataztej_g2_gestionstationski:latest"


        APP_IMAGE = "moataztej_g2_gestionstationski:${VERSION}"
    }

    stages {
        stage('Cleanup') {
            steps {
                script {
                    sh 'mvn clean'
                }
            }
        }

        stage('Set-Version') {
            steps {
                script {
		    sh "mvn versions:set -DnewVersion=${VERSION}"
                }
            }
        }

        stage('Compilation') {
            steps {
                script {
                    sh 'mvn compile -DskipTests'
                }
            }
        }


        stage('Unit-Tests') {
            steps {
                script {
                    sh 'mvn verify test -DskipCompile'
                }
            }
        }

        stage('Sonar-Analysis') {
            steps {
		script {
                    sh 'mvn sonar:sonar -Dsonar.host.url=http://sonar:9000'
                }
            }
        }

        stage('Packaging') {
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

        stage('Image-Build') {
            steps {
                script {
                    sh "docker build -t ${APP_IMAGE} ."
                }
            }
        }

        stage('Tag Docker Image') {
            steps {
                script {
		    sh '''
		    docker tag "$APP_IMAGE" "$DOCKER_REPOSITORY"
		    docker tag "$APP_IMAGE" "$DOCKER_REPOSITORY_LATEST"
		    
                    '''
                }
            }
        }

        stage('Push Image to Docker Hub') {
            steps {
                script {
		    sh '''
                    echo "$DOCKERHUB_PASSWORD" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
		    docker push "$DOCKER_REPOSITORY"
		    docker push "$DOCKER_REPOSITORY_LATEST"
                    '''
                }
            }
        }

        stage('Stop and Remove Containers') {
            steps {
                script {
                    sh 'docker-compose down'
                }
            }
        }

        stage('Delete Old Docker Image') {
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

        stage('Pull Images and Restart Containers') {
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
