workspace "Structurizr on-premises installation" "The software architecture model for the Structurizr on-premises installation." {

    model {
        anonymousUser = person "Anonymous User" "An anonymous, unauthenticated, user."
        workspaceReader = person "Workspace Reader" "An authenticated user with read-only access to one or more workspaces."
        workspaceWriter = person "Workspace Writer" "An authenticated user with read/write access to one or more workspaces."
        adminUser = person "Admin User" "An authenticated user with admin access."
        
        structurizr = softwareSystem "Structurizr on-premises installation" "Your team's software architecture documentation hub." {
            tags "Structurizr on-premises installation"

            structurizrWeb = container "structurizr-onpremises" "Serves HTML/CSS/JavaScript content to users, and hosts the Structurizr web API." "Java/Spring MVC web app"
            structurizrData = container "structurizr-data" "File-based data storage for on-premises data and log files." "File System" {
                tags "File System"
            }
            elasticsearch = container "Distributed search index" "Search indexes for Structurizr workspaces." "Elasticsearch index" {
                tags "Elasticsearch, Optional"
            } 
            s3 = container "Object store" "Stores workspace and review data." "Amazon Web Services S3" "Object Store, Optional"
        }

        structurizrClient = softwareSystem "Structurizr client" "Creates a software architecture model via the Web API, using the Structurizr CLI or a client library (Java, .NET, TypeScript, PHP, Python, etc)." "" 

        idp = softwareSystem "Identity Provider" {
            tags "Optional"
        }
        
        anonymousUser -> structurizrWeb "Views public/shareable workspaces using" 
        workspaceWriter -> Structurizrclient "Creates and manages workspaces using" 
        workspaceWriter -> structurizrWeb "Views and updates workspaces using"
        workspaceReader -> structurizrWeb "Views workspaces using"
        adminUser -> structurizrWeb "Views, updates, and manages workspaces using"

        structurizrClient -> structurizrWeb "Reads/writes workspaces using" "JSON/HTTPS"
        structurizrWeb -> structurizrData "Reads from and writes to"
        structurizrWeb -> elasticsearch "Reads from and writes to" "HTTPS"
        structurizrWeb -> s3 "Reads from and writes to" "HTTPS"

        structurizrWeb -> idp "Uses for authentication and/or authorisation" "LDAP or SAML 2.0"
        
        deploymentEnvironment "Example1" {
            deploymentNode "Customer data center, private cloud, or public cloud" {
                deploymentNode "Windows or Linux server" {
                    containerInstance structurizrData
                    deploymentNode "Docker" {
                        deploymentNode "Apache Tomcat" {
                            containerInstance structurizrWeb
                        }
                    }
                }
            }
        }

        deploymentEnvironment "Example2" {
            deploymentNode "Customer data center, private cloud, or public cloud" {
                deploymentNode "Windows or Linux server 1" {
                    deploymentNode "Docker" {
                        deploymentNode "Apache Tomcat" {
                            containerInstance structurizrWeb
                        }
                    }
                }
    
                deploymentNode "Windows or Linux server 2" {
                    deploymentNode "Docker" {
                        deploymentNode "Apache Tomcat" {
                            containerInstance structurizrWeb
                        }
                    }
                }
    
                deploymentNode "Elasticsearch cluster" {
                    containerInstance elasticsearch
                }
    
                deploymentNode "Network File Share" {
                    containerInstance structurizrData
                }
            }
        }
        
        deploymentEnvironment "Example4" {
            instance1 = deploymentGroup "Instance 1"
            instance2 = deploymentGroup "Instance 2"

            deploymentNode "Amazon Web Services" {
                deploymentNode "Windows or Linux server 1" {
                    deploymentNode "Docker" {
                        deploymentNode "Apache Tomcat" {
                            containerInstance structurizrWeb instance1
                            containerInstance structurizrData instance1
                        }
                    }
                }
    
                deploymentNode "Windows or Linux server 2" {
                    deploymentNode "Docker" {
                        deploymentNode "Apache Tomcat" {
                            containerInstance structurizrWeb instance2
                            containerInstance structurizrData instance2
                        }
                    }
                }
    
                deploymentNode "Elasticsearch cluster" {
                    containerInstance elasticsearch instance1,instance2
                }
                
                deploymentNode "S3" {
                    containerInstance s3 instance1,instance2
                }
            }
        }
        
        deploymentEnvironment "Example3" {
            deploymentNode "Customer data center, private cloud, or public cloud" {
                deploymentNode "Windows or Linux server" {
                    containerInstance structurizrData
                    deploymentNode "Docker" {
                        deploymentNode "Apache Tomcat" {
                            containerInstance structurizrWeb
                        }
                    }
                }
            }
            deploymentNode "Auth0, Okta, Microsoft Azure, OneLogin, etc" {
                softwareSystemInstance idp
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
        
        deployment structurizr "Example1" "Deployment-Example1" "An example single-server installation." {
            include *
        }

        deployment structurizr "Example2" "Deployment-Example2" "An example multi-server installation, with Elasticsearch" {
            include *
        }
        
        deployment structurizr "Example3" "Deployment-Example3" "An example single-server installation, with SAML 2.0 integration." {
            include *
        }

        deployment structurizr "Example4" "Deployment-Example4" "An example multi-server installation, with Elasticsearch and AWS S3." {
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
            element "File System" {
                shape "Folder" 
            }
            element "Object Store" {
                shape "Folder" 
            }
            element "Person" {
                shape "Person" 
                background "#08427b" 
                color "#ffffff" 
            }
            element "Elasticsearch" {
                shape Cylinder
            }
            element "Optional" {
                opacity 50
            }
        }

    }

}