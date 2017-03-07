Vagrant.require_version ">= 1.9.1"

Vagrant.configure("2") do |config|

  config.vm.define "droplet1" do |config|
    config.vm.provider :digital_ocean do |provider, override|
      override.ssh.private_key_path = "~/.ssh/digitalocean"
      override.vm.box = "digital_ocean"
      override.vm.box_url = "https://github.com/devops-group-io/vagrant-digitalocean/raw/master/box/digital_ocean.box"
      provider.token = "c6982988226dd3fd6fd7466579df250e81bd785fa9bc0c73ff8e57c9c9179683"
      provider.image = "debian-8-x64"
      provider.region = "ams2"
      provider.size = "512mb"
    end
  end

  config.vm.synced_folder "data", "/vagrant/data",
    id: "data",
    create: true,
    owner: root,
    group: root

  config.vm.provision "ansible" do |ansible|
    ansible.playbook = "vagrant/playbook.yml"
  end

end