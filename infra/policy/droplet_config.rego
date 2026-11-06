package terraform.security

import rego.v1

# Planned (non-destroyed) droplets.
droplets contains rc if {
	some rc in input.resource_changes
	rc.type == "digitalocean_droplet"
	rc.change.after != null
}

approved_images := {"ubuntu-22-04-x64", "debian-12-x64"}

# --- Policy 3: Enforce the use of approved images ---
deny contains msg if {
	some rc in droplets
	not rc.change.after.image in approved_images
	msg := sprintf("Droplet %s uses a disallowed image %s. Approved images are: %s.", [rc.change.after.name, rc.change.after.image, concat(", ", sort(approved_images))])
}

# --- Policy 4: Enforce SSH key-based authentication ---
# (skipped when the key list is only known after apply)
deny contains msg if {
	some rc in droplets
	not object.get(rc.change.after_unknown, "ssh_keys", false)
	count(object.get(rc.change.after, "ssh_keys", [])) == 0
	msg := sprintf("Droplet %s must have at least one SSH key configured.", [rc.change.after.name])
}

# --- Policy 5: Droplets must carry the tags the firewalls select on ---
# Firewalls scope SSH access with the tag role:bastion-<env> (see
# modules/stack/main.tf), so a bastion droplet without that tag silently breaks
# the "SSH only via bastion" design. Every droplet must also carry an env: tag.
# (Software such as fail2ban is installed by Ansible, not cloud-init, so it is
# intentionally not checked here.)
deny contains msg if {
	some rc in droplets
	endswith(rc.change.after.name, "-bastion")
	not has_tag_prefix(rc, "role:bastion-")
	msg := sprintf("Bastion droplet %s must be tagged role:bastion-<env>.", [rc.change.after.name])
}

deny contains msg if {
	some rc in droplets
	not has_tag_prefix(rc, "env:")
	msg := sprintf("Droplet %s must be tagged env:<env>.", [rc.change.after.name])
}

has_tag_prefix(rc, prefix) if {
	some tag in rc.change.after.tags
	startswith(tag, prefix)
}
