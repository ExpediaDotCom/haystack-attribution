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
variable "environment_name" {}
variable "attribution_to_email_ids" {}
variable "attribution_from_email_id" {}
variable "attributor_tags" {}
variable "attribution_email_subject_line" {}
variable "attribution_email_override_template" {}

variable "termination_grace_period" {
  default = 30
}

variable "service_port" {
  default = 8080
}
variable "container_port" {
  default = 8080
}
