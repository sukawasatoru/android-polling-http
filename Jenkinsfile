pipeline {
    agent any

    stages {
        stage('Preparing') {
            steps {
                fileExists '.git'
                sh 'git clean -fdx'
                git(url: 'https://github.com/sukawasatoru/android-polling-http', branch: 'master', changelog: true)
            }
        }

        stage('Build') {
            steps {
                sh './gradlew --info --profile --no-daemon build'
            }
        }

        stage('Report') {
            steps {
                parallel(
                        "Report": {
                            checkstyle(canComputeNew: true, canResolveRelativePaths: true)
                        },
                        "Artifact": {
                            archiveArtifacts(artifacts: '**/*.apk,**/reports/lint-results*', fingerprint: true)
                        }
                )
            }
        }
    }
}
