workspace "Structurizr on-premises installation" "The software architecture model for the Structurizr on-premises installation." {

    !identifiers hierarchical
    !const STRUCTURIZR_ONPREMISES_HOME "/Users/simon/sandbox/structurizr-onpremises"

    !docs docs

    model {
        anonymousUser = person "Anonymous User" "An anonymous, unauthenticated, user."
        workspaceReader = person "Workspace Reader" "An authenticated user with read-only access to one or more workspaces."
        workspaceWriter = person "Workspace Writer" "An authenticated user with read/write access to one or more workspaces."
        adminUser = person "Admin User" "An authenticated user with admin access."

        structurizrLite = softwareSystem "Structurizr Lite" "Standalone tool to view/edit diagrams, view documentation, and view architecture decision records defined in workspace."
        structurizrCli = softwareSystem "Structurizr CLI" "A command line utility designed to be used in conjunction with the Structurizr DSL."
        structurizrClient = softwareSystem "Structurizr client" "Other Structurizr-compatible client (e.g. using structurizr-java)."

        idp = softwareSystem "Identity Provider (optional)" {
            tags "Optional"
        }

        structurizr = softwareSystem "Structurizr on-premises installation" "Your team's software architecture documentation hub." {
            tags "Structurizr on-premises installation"

            ui = container "structurizr-ui" {
                tags "Browser App"
            }

            workspaceData = container "Workspace Data" "Stores workspace.json files." {
                tag "Data"
            }
            reviewData = container "Review Data" "Stores diagram review data." {
                tag "Data"
            }
            searchIndexData = container "Search Indexes" "Stores search indexes." {
                tag "Data"
            }

            structurizrWeb = container "structurizr-onpremises" "Serves HTML/CSS/JavaScript content to users, and hosts the Structurizr web API." "Java/Spring MVC web app" {
                !components {
                    classes "${STRUCTURIZR_ONPREMISES_HOME}/structurizr-onpremises/build/libs/structurizr-onpremises.jar"
                    source "${STRUCTURIZR_ONPREMISES_HOME}/structurizr-onpremises/src/main/java"
                    strategy {
                        technology "Java Component"
                        matcher name-suffix "Component"
                        supportingTypes in-package
                        url prefix-src "https://github.com/structurizr/onpremises/blob/main/structurizr-onpremises/src/main/java/"
                    }
                    strategy {
                        technology "Spring MVC Controller"
                        matcher annotation "org.springframework.stereotype.Controller"
                        filter exclude fqn-regex ".*AbstractController|.*.Http[0-9]*Controller"
                        url prefix-src "https://github.com/structurizr/onpremises/blob/main/structurizr-onpremises/src/main/java/"
                        forEach {
                            -> ui "Renders HTML page in"
                        }
                    }
                    strategy {
                        technology "Spring MVC REST Controller"
                        matcher annotation "org.springframework.web.bind.annotation.RestController"
                        filter exclude fqn-regex ".*BcryptController"
                        description first-sentence
                        url prefix-src "https://github.com/structurizr/onpremises/blob/main/structurizr-onpremises/src/main/java/"
                        forEach {
                            tag "API Component"
                        }
                    }
                }

                !script groovy {
                    element.components.findAll { it.properties["component.type"].matches("com\\.structurizr\\.onpremises\\.web\\.workspace\\.explore\\..*") }.each { it.addTags("Feature:Explore") }
                    element.components.findAll { it.properties["component.type"].matches("com\\.structurizr\\.onpremises\\.web\\.workspace\\.management\\..*") }.each { it.addTags("Feature:WorkspaceManagement") }
                }

                ui -> workspaceApiController "Reads/writes workspaces using" "JSON/HTTPS"
                ui -> graphvizController "Requests automatic layout information from" "JSON/HTTPS"
                structurizrLite -> workspaceApiController "Reads/writes workspaces using" "JSON/HTTPS"
                structurizrCli -> workspaceApiController "Reads/writes workspaces using" "JSON/HTTPS"
                structurizrClient -> workspaceApiController "Reads/writes workspaces using" "JSON/HTTPS"
                structurizrClient -> adminApiController "Manages workspaces using" "JSON/HTTPS"
                ui -> embedController "Embeds diagrams using"
            }
        }

        anonymousUser -> structurizr.ui "Views public/shareable workspaces using"
        workspaceWriter -> structurizr.ui "Views and updates workspaces using"
        workspaceReader -> structurizr.ui "Views workspaces using"
        adminUser -> structurizr.ui "Views, updates, and manages workspaces using"

        structurizr.structurizrWeb.workspaceComponent -> structurizr.workspaceData "Reads from and writes to"
        structurizr.structurizrWeb.reviewComponent -> structurizr.reviewData "Reads from and writes to"
        structurizr.structurizrWeb.searchComponent -> structurizr.searchIndexData "Reads from and writes to"

        structurizr.structurizrWeb -> idp "Uses for authentication and/or authorisation" "LDAP or SAML 2.0"
        
        deploymentEnvironment "Example1" {
            deploymentNode "End-user's computer" {
                deploymentNode "Web Browser" {
                    containerInstance structurizr.ui
                }
            }
            deploymentNode "On-premises or cloud environment" {
                deploymentNode "Windows or Linux server" {
                    deploymentNode "Local File System" {
                        containerInstance structurizr.workspaceData
                        containerInstance structurizr.reviewData
                        containerInstance structurizr.searchIndexData
                    }
                    deploymentNode "Apache Tomcat" {
                        containerInstance structurizr.structurizrWeb
                    }
                }
            }
        }

        deploymentEnvironment "Example2" {
            deploymentNode "On-premises or cloud environment" {
                loadBalancer = infrastructureNode "Load Balancer"

                deploymentNode "Windows or Linux server 1" {
                    deploymentNode "Apache Tomcat" {
                        containerInstance structurizr.structurizrWeb {
                            loadBalancer -> this "Forwards requests to"
                        }
                    }
                }
    
                deploymentNode "Windows or Linux server 2" {
                    deploymentNode "Apache Tomcat" {
                        containerInstance structurizr.structurizrWeb {
                            loadBalancer -> this "Forwards requests to"
                        }
                    }
                }

                deploymentNode "Elasticsearch cluster" {
                    containerInstance structurizr.searchIndexData
                }
    
                deploymentNode "Network File Share" {
                    containerInstance structurizr.workspaceData
                    containerInstance structurizr.reviewData
                }
            }
        }

        deploymentEnvironment "Example3" {
            deploymentNode "Amazon Web Services" {
                loadBalancer = infrastructureNode "Application Load Balancer"

                deploymentNode "Windows or Linux server 1" {
                    deploymentNode "Apache Tomcat" {
                        containerInstance structurizr.structurizrWeb {
                            loadBalancer -> this "Forwards requests to"
                        }
                    }
                }
    
                deploymentNode "Windows or Linux server 2" {
                    deploymentNode "Apache Tomcat" {
                        containerInstance structurizr.structurizrWeb {
                            loadBalancer -> this "Forwards requests to"
                        }
                    }
                }
    
                deploymentNode "Elasticsearch cluster" {
                    containerInstance structurizr.searchIndexData
                }
                
                deploymentNode "S3" {
                    containerInstance structurizr.workspaceData
                    containerInstance structurizr.reviewData
                }
            }
        }

    }

    views {
        systemContext structurizr "SystemContext" {
            include *
        }

        container structurizr "Containers" {
            include *
        }

        component structurizr.structurizrWeb "Components-Diagrams" {
            description "Component diagram for the diagrams functionality."
            include ->structurizr.structurizrWeb.diagramViewerController->
            include ->structurizr.structurizrWeb.diagramEditorController->
            include structurizr.structurizrWeb.graphvizController
            include structurizr.structurizrWeb.workspaceApiController
            include structurizr.ui
            include structurizr.workspaceData
        }

        component structurizr.structurizrWeb "Components-Documentation" {
            description "Component diagram for the documentation functionality."
            include ->structurizr.structurizrWeb.documentationController->
            include structurizr.structurizrWeb.embedController
            include structurizr.structurizrWeb.workspaceApiController
            include structurizr.workspaceData
        }

        component structurizr.structurizrWeb "Components-Decisions" {
            description "Component diagram for the decisions functionality."
            include ->structurizr.structurizrWeb.decisionsController->
            include structurizr.structurizrWeb.embedController
            include structurizr.structurizrWeb.workspaceApiController
            include structurizr.workspaceData
        }

        component structurizr.structurizrWeb "Components-Explore" {
            description "Component diagram for the explore functionality."
            include "->element.tag==Feature:Explore->"
            include structurizr.workspaceData
        }

        component structurizr.structurizrWeb "Components-WorkspaceManagement" {
            description "Component diagram for the workspace management functionality."
            include "->element.tag==Feature:WorkspaceManagement->"
            include structurizr.workspaceData
            include structurizr.searchIndexData
        }

        component structurizr.structurizrWeb "Components-WorkspaceApi" {
            description "Component diagram for the workspace API functionality."
            include ->structurizr.structurizrWeb.workspaceApiController->
            include structurizr.workspaceData
            include structurizr.searchIndexData
            autolayout
        }

        component structurizr.structurizrWeb "Components-AdminApi" {
            description "Component diagram for the admin API functionality."
            include ->structurizr.structurizrWeb.adminApiController->
            include structurizr.workspaceData
            autolayout
        }

        deployment structurizr "Example1" "Deployment-Example1" "An example single-server installation." {
            include *
        }

        deployment structurizr "Example2" "Deployment-Example2" "An example multi-server installation, with Elasticsearch" {
            include *
        }

        deployment structurizr "Example3" "Deployment-Example4" "An example multi-server installation, with Elasticsearch and AWS S3." {
            include *
        }

        styles {
            element "Software System" {
                shape "RoundedBox" 
                background "#dddddd" 
                color "#000000" 
            }
            element "Structurizr on-premises installation" {
                background "#1168bd" 
                color "#ffffff" 
            }
            element "Container" {
                shape "RoundedBox" 
                background "#438dd5" 
                color "#ffffff" 
            }
            element "Data" {
                shape "Folder" 
            }
            element "Person" {
                shape "Person" 
                background "#08427b" 
                color "#ffffff" 
            }
            element "Optional" {
                opacity 50
            }
        }

    }

}