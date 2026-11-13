package terraform.security_test

# Unit tests for the terraform.security policies, run with `opa test infra/policy`.
# They use small hand-written plan fragments, so they need no cloud credentials.

import data.terraform.security
import rego.v1

fw(name, rules) := {
	"address": sprintf("module.staging.digitalocean_firewall.%s", [name]),
	"type": "digitalocean_firewall",
	"change": {"after": {"name": name, "inbound_rule": rules}},
}

ssh_from(cidrs) := {"protocol": "tcp", "port_range": "22", "source_addresses": cidrs}

droplet(name, image, keys, tags) := {
	"address": sprintf("module.staging.digitalocean_droplet.%s", [name]),
	"type": "digitalocean_droplet",
	"change": {"after": {"name": name, "image": image, "ssh_keys": keys, "tags": tags}, "after_unknown": {}},
}

plan(rcs) := {"resource_changes": rcs}

good_bastion := droplet("staging-bastion", "ubuntu-22-04-x64", [123], ["env:staging", "role:bastion-staging"])

good_frontend := droplet("staging-frontend", "ubuntu-22-04-x64", [123], ["env:staging"])

# --- A compliant plan produces no violations ---
test_compliant_plan_passes if {
	count(security.deny) == 0 with input as plan([
		good_bastion,
		good_frontend,
		fw("staging-bastion-fw", [ssh_from(["203.0.113.10/32"])]),
		fw("staging-frontend-fw", [ssh_from(["10.10.0.0/16"])]),
	])
}

# --- Policy 1: no public SSH outside the bastion ---
test_public_ssh_on_frontend_denied if {
	count(security.deny) == 1 with input as plan([fw("staging-frontend-fw", [ssh_from(["0.0.0.0/0"])])])
}

test_public_http_on_frontend_allowed if {
	count(security.deny) == 0 with input as plan([fw(
		"staging-frontend-fw",
		[{"protocol": "tcp", "port_range": "443", "source_addresses": ["0.0.0.0/0", "::/0"]}],
	)])
}

# --- Policy 2: bastion SSH must come from explicit admin CIDRs ---
test_public_ssh_on_bastion_denied if {
	count(security.deny) == 1 with input as plan([fw("staging-bastion-fw", [ssh_from(["::/0"])])])
}

test_bastion_ssh_without_source_denied if {
	count(security.deny) == 1 with input as plan([fw("staging-bastion-fw", [ssh_from([])])])
}

# --- Policy 3: approved images only ---
test_unapproved_image_denied if {
	count(security.deny) == 1 with input as plan([droplet("staging-frontend", "centos-7-x64", [123], ["env:staging"])])
}

# --- Policy 4: SSH keys required (unless only known after apply) ---
test_droplet_without_ssh_key_denied if {
	count(security.deny) == 1 with input as plan([droplet("staging-frontend", "ubuntu-22-04-x64", [], ["env:staging"])])
}

test_ssh_keys_unknown_at_plan_allowed if {
	d := json.patch(
		droplet("staging-frontend", "ubuntu-22-04-x64", [], ["env:staging"]),
		[{"op": "add", "path": "/change/after_unknown/ssh_keys", "value": true}],
	)
	count(security.deny) == 0 with input as plan([d])
}

# --- Policy 5: required tags ---
test_bastion_without_role_tag_denied if {
	count(security.deny) == 1 with input as plan([droplet("staging-bastion", "ubuntu-22-04-x64", [123], ["env:staging"])])
}

test_droplet_without_env_tag_denied if {
	count(security.deny) == 1 with input as plan([droplet("staging-frontend", "ubuntu-22-04-x64", [123], [])])
}

# --- Destroyed resources are ignored ---
test_destroyed_firewall_ignored if {
	count(security.deny) == 0 with input as plan([{
		"address": "module.staging.digitalocean_firewall.old",
		"type": "digitalocean_firewall",
		"change": {"after": null},
	}])
}
