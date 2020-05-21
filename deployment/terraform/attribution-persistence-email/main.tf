locals {
  app_name = "attribution-email"
  image = "${var.image}"
  config_file_path = "${path.module}/templates/attribution-email.conf"
  deployment_yaml_file_path = "${path.module}/templates/deployment.yaml"
  count = "${var.enabled?1:0}"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "attribution-email-${local.checksum}"
}

resource "kubernetes_config_map" "haystack-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "attribution-email.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"

}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    environment_name = "${var.environment_name}"
    attribution_to_email_ids = "${var.attribution_to_email_ids}"
    attribution_from_email_id = "${var.attribution_from_email_id}"
    attributor_tags = "${var.attributor_tags}"
    attribution_email_subject_line = "${var.attribution_email_subject_line}"
    attribution_email_override_template = "${var.attribution_email_override_template}"
  }
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    graphite_port = "${var.graphite_port}"
    graphite_host = "${var.graphite_hostname}"
    graphite_enabled = "${var.graphite_enabled}"
    node_selecter_label = "${var.node_selector_label}"
    image = "${var.image}"
    replicas = "${var.replicas}"
    memory_limit = "${var.memory_limit}"
    memory_request = "${var.memory_request}"
    jvm_memory_limit = "${var.jvm_memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    cpu_request = "${var.cpu_request}"
    service_port = "${var.service_port}"
    container_port = "${var.container_port}"
    configmap_name = "${local.configmap_name}"
    env_vars= "${indent(9,"${var.env_vars}")}"
  }
}

resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  count = "${local.count}"
}


resource "null_resource" "kubectl_destroy" {

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
}
