package terraform.security

import rego.v1

# Notes on matching
# - Resources live inside modules, so their plan address is e.g.
#   module.staging.digitalocean_firewall.frontend_fw; match on rc.type / rc.name
#   (or on the planned resource name "<env>-<role>-fw"), never on Terraform
#   expressions such as digitalocean_droplet.frontend.id.
# - droplet_ids is unknown at plan time (computed), so firewalls are identified
#   by their planned name.

public_cidrs := {"0.0.0.0/0", "::/0"}

# Planned (non-destroyed) firewalls.
firewalls contains rc if {
	some rc in input.resource_changes
	rc.type == "digitalocean_firewall"
	rc.change.after != null
}

ssh_rule(rule) if {
	rule.protocol == "tcp"
	rule.port_range == "22"
}

# --- Policy 1: Bastion host is the only public SSH entry ---
# No firewall (frontend, backend, ...) may expose SSH to the public internet.
deny contains msg if {
	some rc in firewalls
	not endswith(rc.change.after.name, "-bastion-fw")
	some rule in rc.change.after.inbound_rule
	ssh_rule(rule)
	some cidr in rule.source_addresses
	cidr in public_cidrs
	msg := sprintf("Public SSH access is forbidden on firewall %s (%s). Use the bastion host.", [rc.change.after.name, rc.address])
}

# --- Policy 2: Strict SSH source restrictions on the bastion ---
# The bastion firewall may only allow SSH from explicit admin CIDRs: no public
# CIDR, and no SSH rule without any source.
deny contains msg if {
	some rc in firewalls
	endswith(rc.change.after.name, "-bastion-fw")
	some rule in rc.change.after.inbound_rule
	ssh_rule(rule)
	some cidr in rule.source_addresses
	cidr in public_cidrs
	msg := sprintf("Bastion firewall %s must not allow SSH from the public internet (%s).", [rc.change.after.name, cidr])
}

deny contains msg if {
	some rc in firewalls
	endswith(rc.change.after.name, "-bastion-fw")
	some rule in rc.change.after.inbound_rule
	ssh_rule(rule)
	count(object.get(rule, "source_addresses", [])) == 0
	msg := sprintf("Bastion firewall %s must have a source IP CIDR for SSH access.", [rc.change.after.name])
}
