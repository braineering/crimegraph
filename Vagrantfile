Vagrant.require_version ">= 1.9.1"

Vagrant.configure("2") do |config|

  config.vm.define "crimegraph" do |config|
    config.vm.provider :digital_ocean do |provider, override|
      override.ssh.private_key_path = "~/.ssh/crimegraph.digitalocean.com"
      override.vm.box = "digital_ocean"
      override.vm.box_url = "https://github.com/devops-group-io/vagrant-digitalocean/raw/master/box/digital_ocean.box"
      provider.token = "d9367e99a669643ad233106acdcceb38d6046844f2e4bceeb14063a3812bd587"
      provider.image = "debian-8-x64"
      provider.region = "ams2"
      provider.size = "512mb"
    end
  end

  config.vm.synced_folder ".", "/vagrant",
    id: "default",
    disabled: true,
    type: "rsync",
    create: true

  config.vm.synced_folder "data/", "/vagrant/data",
    id: "data",
    disabled: false,
    type: "rsync",
    create: true

  config.vm.synced_folder "target/", "/vagrant/target",
    id: "target",
    disabled: false,
    type: "rsync",
    create: true

  config.vm.synced_folder "site/", "/vagrant/site",
    id: "target",
    disabled: false,
    type: "rsync",
    create: true

  config.vm.provision "ansible" do |ansible|
    ansible.playbook = "ansible/playbook.yml"
  end

end
