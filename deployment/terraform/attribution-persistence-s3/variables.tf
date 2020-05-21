variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "enabled"{}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selector_label"{}
variable "memory_request"{}
variable "memory_limit"{}
variable "cpu_request"{}
variable "cpu_limit"{}
variable "jvm_memory_limit"{}
variable "env_vars" {}
variable "attribution_s3_transformers_config" {}
variable "attribution_s3_aws_config" {}
variable "attributor_tags" {}
variable "environment_name" {}

variable "termination_grace_period" {
  default = 30
}

variable "service_port" {
  default = 8080
}
variable "container_port" {
  default = 8080
}
