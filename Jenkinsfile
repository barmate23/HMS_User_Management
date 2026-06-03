pipeline {
    agent any

    environment {
        REGISTRY_CONTAINER_NAME = "adminserviceregistry"

        TARGET_CONTAINER_NAME = "userservice"
        TARGET_IMAGE_NAME = "userservice:latest"

        DOCKER_NETWORK = "updated_orgadmin_rmscadminnetwork"

        HOST_PORT = "9094"
        CONTAINER_PORT = "9094"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Version') {
            steps {
                sh 'docker --version'
            }
        }

        stage('Check Registry') {
            steps {
                script {
                    def isRegistryRunning = sh(
                        script: "docker ps -q -f name=${REGISTRY_CONTAINER_NAME}",
                        returnStdout: true
                    ).trim()

                    if (!isRegistryRunning) {
                        error "${REGISTRY_CONTAINER_NAME} is not running. Aborting deployment."
                    }

                    echo "${REGISTRY_CONTAINER_NAME} is running. Proceeding..."
                }
            }
        }

        stage('Check Docker Network') {
            steps {
                sh """
                    docker network inspect ${DOCKER_NETWORK} >/dev/null 2>&1 || \
                    docker network create ${DOCKER_NETWORK}
                """
            }
        }

        stage('Remove Existing Container and Image') {
            steps {
                sh "docker rm -f ${TARGET_CONTAINER_NAME} || true"
                sh "docker rmi -f ${TARGET_IMAGE_NAME} || true"
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "DOCKER_BUILDKIT=0 docker build --no-cache -t ${TARGET_IMAGE_NAME} ."
            }
        }

        stage('Run Container') {
            steps {
                sh """
                    docker run -d \
                    --name ${TARGET_CONTAINER_NAME} \
                    --network ${DOCKER_NETWORK} \
                    -p ${HOST_PORT}:${CONTAINER_PORT} \
                    --restart unless-stopped \
                    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://${REGISTRY_CONTAINER_NAME}:8761/eureka \
                    -e EUREKA_CLIENT_REGISTER_WITH_EUREKA=true \
                    -e EUREKA_CLIENT_FETCH_REGISTRY=true \
                    ${TARGET_IMAGE_NAME}
                """
            }
        }

        stage('Health Check') {
            steps {
                script {
                    sleep(time: 20, unit: 'SECONDS')

                    def containerId = sh(
                        script: "docker ps -q -f name=${TARGET_CONTAINER_NAME}",
                        returnStdout: true
                    ).trim()

                    if (!containerId) {
                        sh "docker logs ${TARGET_CONTAINER_NAME} || true"
                        error("Container failed to start.")
                    }

                    echo "✅ ${TARGET_CONTAINER_NAME} deployed successfully."
                }
            }
        }
    }

    post {
        always {
            echo '✅ Pipeline execution completed.'
        }

        failure {
            echo '❌ Deployment failed.'
        }

        success {
            echo '🚀 Deployment successful.'
        }
    }
}
