pipeline {
    agent any

    tools {
        maven 'Maven 3.9.9'
    }

    environment {
        DOCKERHUB_USERNAME = credentials('DOCKERHUB_USERNAME')
        DOCKERHUB_PASSWORD = credentials('DOCKERHUB_PASSWORD')
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        NEXUS_USERNAME = credentials('NEXUS_USERNAME')
        NEXUS_PASSWORD = credentials('NEXUS_PASSWORD')
    }

    stages {
        stage('Clean') {
            steps {
                script {
                    sh 'mvn clean'
                }
            }
        }

        stage('Versioning') {
            steps {
                script {
		    sh "mvn versions:set -DnewVersion=1.0.${BUILD_NUMBER}-SNAPSHOT"
                }
            }
        }

        stage('Compile') {
            steps {
                script {
                    sh 'mvn compile -DskipTests'
                }
            }
        }


        stage('Junit & Mockito') {
            steps {
                script {
                    sh 'mvn verify test -DskipCompile'
                }
            }
        }

        stage('Sonar-Test') {
            steps {
		script {
                    sh 'mvn sonar:sonar -Dsonar.host.url=http://sonar:9000'
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    sh 'mvn package -DskipTests -DskipCompile'
                }
            }
        }

        stage('Deploy-Nexus') {
            steps {
                script {
		    sh "mvn deploy -DskipTests -DskipCompile -DskipPackaging -s mvn-settings.xml -P snapshot"
                }
            }
        }

        stage('Build-Image') {
            steps {
                script {
                    sh 'docker build -t armenbakir_g2_gestionstationski:1.0.${BUILD_NUMBER}-SNAPSHOT .'
                }
            }
        }
        stage('Push-Image-Dockerhub') {
            steps {
                script {
		    sh '''
                    echo "$DOCKERHUB_PASSWORD" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
                    '''
                }
            }
        }
        stage('Deploy-Container') {
            steps {
                script {
                    sh 'docker-compose down && docker-compose up -d'
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
