#*******************************************************************************
# PACKAGES
#*******************************************************************************
require "vagrant-aws"
require "yaml"

#*******************************************************************************
# REQUIREMENTS
#*******************************************************************************
Vagrant.require_version ">= 1.9.1"
VAGRANTFILE_API_VERSION = "2"
ENV["VAGRANT_DEFAULT_PROVIDER"] = "aws"
PROJECT_NAME = "crimegraph"

inventory = YAML.load_file(File.join(File.dirname(__FILE__), "inventory.yaml"))

ansible_groups = {}
inventory.each do |cluster|
  ansible_groups[cluster["name"]] = []
  cluster["instances"].each do |instance|
    ansible_groups[cluster["name"]] << instance["name"]
  end
end

#*******************************************************************************
# CONFIGURATION
#*******************************************************************************
Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "dummy"

  config.vm.provider :aws do |aws, override|
    aws.access_key_id = ENV["AWS_ACCESS_KEY_ID"]
    aws.secret_access_key = ENV["AWS_SECRET_ACCESS_KEY"]
    aws.keypair_name = ENV["AWS_KEYPAIR_NAME"]

    aws.region = "eu-west-1"
  end # config.vm.provider aws

  inventory.each do |cluster|
    cluster["instances"].each do |instance|
      config.vm.define instance["name"] do |srv|
        srv.vm.provider :aws do |aws, override|
          aws.instance_type = instance["type"]
          aws.ami = instance["ami"]
          aws.security_groups = instance["security_groups"]
          aws.tags = {
            "Name" => instance["name"],
            "Project" => PROJECT_NAME
          }
          override.ssh.username = instance["user"]
          override.ssh.private_key_path = ENV["AWS_KEYPAIR_PATH"]
        end # config.vm.provider aws

        srv.vm.synced_folder ".", "/vagrant", disabled: true

        srv.vm.synced_folder "data/common", "/vagrant/data/common",
          id: "data_common",
          disabled: false,
          type: "rsync",
          create: true

        srv.vm.synced_folder "data/" + cluster["name"] + "/common", "/vagrant/data/cluster",
          id: "data_cluster",
          disabled: false,
          type: "rsync",
          create: true

        srv.vm.synced_folder "data/" + cluster["name"] + "/" + instance["name"], "/vagrant/data/instance",
          id: "data_instance",
          disabled: false,
          type: "rsync",
          create: true

        if instance["name"] === "flink_master" then
          srv.vm.synced_folder "target", "/vagrant/target",
            id: "target",
            disabled: false,
            type: "rsync",
            create: true
        end # if instance["name"] === "flink_master"
      end # config.vm.define instance[name]
    end # cluster.each do instance
  end # inventory.each do cluster

  config.vm.provision :ansible do |ansible|
    ansible.playbook = "ansible/playbook.yml"
    ansible.groups = ansible_groups
    ansible.verbose = true
  end # config.vm.provision ansible

end # Vagrant.configure
