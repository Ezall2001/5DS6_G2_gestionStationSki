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
	DOCKER_REPOSITORY_NAME = 'rayenklai_g2_gestionstationski'
	DOCKER_REPOSITORY_NAMESPACE = 'rayenklai200'
	DOCKER_REPOSITORY = "${DOCKER_REPOSITORY_NAMESPACE}/${DOCKER_REPOSITORY_NAME}:${VERSION}"

	APP_IMAGE = "${DOCKER_REPOSITORY_NAME}:${VERSION}"
    }

    stages {
        stage('Nettoyage') {
            steps {
                script {
                    sh 'mvn clean'
                }
            }
        }

        stage('Versionnage') {
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

        stage('Tests Junit & Mockito') {
            steps {
                script {
                    sh 'mvn verify test -DskipCompile'
                }
            }
        }

        stage('Analyse Sonar') {
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

        stage('Déploiement sur Nexus') {
            steps {
                script {
		    sh "mvn deploy -DskipTests -DskipCompile -DskipPackaging -s mvn-settings.xml -P snapshot"
                }
            }
        }

        stage('Construction de l\'Image') {
            steps {
                script {
                    sh "docker build -t ${APP_IMAGE} ."
                }
            }
        }
        stage('Push de l\'Image sur Dockerhub') {
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
        stage('Déploiement du Conteneur') {
            steps {
                script {
                    sh 'docker-compose down && docker-compose up -d --build'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline terminé !'
        }
        success {
            echo 'La construction a réussi !'
            emailext(
            to: 'klairayen123@gmail.com',
            subject: "Succès de la construction : ${env.JOB_NAME} ${env.BUILD_NUMBER}",
            body: "La construction a réussi.\nVoir les détails à : ${env.BUILD_URL}"
        )
        }
        failure {
            echo 'La construction a échoué.'
            emailext(
            to: 'klairayen123@gmail.com',
            subject: "Échec de la construction : ${env.JOB_NAME} ${env.BUILD_NUMBER}",
            body: "La construction a échoué.\nVoir les détails à : ${env.BUILD_URL}"
        )
        }
    }
}
