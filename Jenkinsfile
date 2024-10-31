pipeline {
    agent any

    tools {
        maven 'Maven 3.9.9'
    }

    stages {
        stage('Clean') {
            steps {
                script {
                    sh 'mvn clean'
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


        stage('Test') {
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
                    sh 'mvn deploy -DskipTests -DskipCompile -DskipPackaging -s mvn-settings.xml'
                }
            }
        }

        stage('Build-Image') {
            steps {
                script {
                    sh 'docker build -t ski_station .'
                }
            }
        }
        stage('Push-Image-Dockerhub') {
            steps {
                script {
                    sh 'placeholder'
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
