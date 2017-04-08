Vagrant.require_version ">= 1.9.1"

Vagrant.configure("2") do |config|

  config.vm.define "crimegraph" do |config|
    config.vm.provider :digital_ocean do |provider, override|
      override.ssh.private_key_path = "~/.ssh/vagrant@digitalocean"
      override.vm.box = "digital_ocean"
      override.vm.box_url = "https://github.com/devops-group-io/vagrant-digitalocean/raw/master/box/digital_ocean.box"
      provider.token = "6fac9c763921a69ddd79f3594ee7394e72a6ba01428f5b0e2a6754ffc1ed486d"
      provider.image = "debian-8-x64"
      provider.region = "ams2"
      provider.size = "8gb"
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
