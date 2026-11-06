# 🚨 Infrastructure Drift Detected

## Overview
Terraform has detected that the actual infrastructure state differs from the expected state defined in your code. This means changes were made outside of the normal Terraform workflow.

## Issue Details
- **Environment**: 
- **Detection Date**: 
- **Severity**: 🔴 High Priority (Production) / 🟡 Medium Priority (Staging)
- **Workflow Run**: [View Full Logs](https://github.com/${{ github.repository }}/actions/runs/${{ github.run_id }})

## What Is Infrastructure Drift?

Infrastructure drift occurs when your actual cloud resources don't match what's defined in your Terraform code. This can happen when:

- 🖱️ **Manual Changes**: Someone modified resources directly in the cloud console
- 🤖 **Auto-scaling**: Services automatically added/removed resources
- 🗑️ **External Deletion**: Resources were deleted outside of Terraform
- ⚙️ **Other Tools**: Another automation tool modified the infrastructure
- 💥 **Failed Deploys**: A Terraform apply failed partway through

## Immediate Actions Required

### Step 1: Investigate the Drift
1. **Review Terraform Plan**: Check the workflow logs for detailed output
2. **Identify Changes**: Look for resources marked as `changed`, `added`, or `destroyed`
3. **Check Recent Activity**: Review cloud provider logs for recent modifications
4. **Timeline Analysis**: When did the drift occur? What changed?

### Step 2: Determine Root Cause
- [ ] Check if changes were made manually in cloud console
- [ ] Review recent deployments that might have failed
- [ ] Look for auto-scaling or automated processes
- [ ] Verify if other teams made infrastructure changes
- [ ] Check for any security incidents that might have caused changes

### Step 3: Choose Resolution Strategy

#### Option A: Apply Terraform (Code is Correct)
If your Terraform code represents the desired state:
```bash
cd infra
terraform init
terraform workspace select [staging|production]
terraform plan  
terraform apply  
```

#### Option B: Update Code (Manual Changes are Correct)
If the manual changes should be preserved:
1. Update your Terraform code to match current infrastructure
2. Test changes in staging first
3. Create PR with infrastructure updates
4. Apply updated code

#### Option C: Investigate Further
If the drift is unexpected or concerning:
1. Don't make changes yet
2. Escalate to senior team members
3. Investigate potential security implications
4. Document findings before proceeding

## Verification Checklist
After resolving the drift:

- [ ] **Re-run Detection**: Verify drift is resolved
- [ ] **Test Infrastructure**: Ensure services are working correctly  
- [ ] **Update Documentation**: Record what caused the drift
- [ ] **Prevent Recurrence**: Implement controls to prevent future drift
- [ ] **Close Issue**: Mark as resolved with summary

## Prevention Strategies

### For Future Drift Prevention:
- 🔒 **Access Controls**: Limit who can make manual infrastructure changes
- 📝 **Change Process**: Require all infrastructure changes go through Terraform
- 🔔 **Monitoring**: Set up alerts for manual resource modifications
- 📚 **Training**: Educate team on proper infrastructure change procedures
- 🤖 **Automation**: Use policy-as-code tools (like OPA/Conftest) to enforce standards

## Escalation Path

### If You Need Help:
1. **Check Documentation**: Review Terraform and infrastructure docs
2. **Team Discussion**: Post in infrastructure/DevOps channel
3. **Senior Review**: Tag infrastructure lead for complex issues
4. **Emergency**: For production issues, follow incident response process

### Priority Levels:
- 🔴 **Critical**: Production services affected - Fix immediately
- 🟠 **High**: Production drift but services stable - Fix within 24h
- 🟡 **Medium**: Staging drift - Fix within 1 week
- 🟢 **Low**: Non-critical resources - Fix when convenient

## Common Drift Scenarios

### Scenario 1: Auto-scaling Event
- **Cause**: Load balancer added/removed instances
- **Solution**: Usually safe to ignore, but verify auto-scaling is working correctly

### Scenario 2: Manual Security Fix
- **Cause**: Someone added security group rule during incident
- **Solution**: Update Terraform code to include the security fix

### Scenario 3: Resource Deletion
- **Cause**: Someone deleted a resource manually
- **Solution**: Run `terraform apply` to recreate the resource

### Scenario 4: Configuration Drift
- **Cause**: Settings were changed in cloud console
- **Solution**: Decide if change should be kept or reverted

## Links and Resources

- 📋 [Infrastructure Repository](https://github.com/${{ github.repository }}/tree/main/infra)
- 🔄 [Terraform Workflow](https://github.com/${{ github.repository }}/actions/workflows/terraform.yaml)
- 📊 [Cloud Provider Console](#) 
- 📖 [Team Infrastructure Docs](#) 
- 🆘 [Incident Response Process](#) 

## Resolution Summary

**Root Cause:** 

**Resolution Action:** 

**Prevention Measures:** 

---

**Next Drift Check:** The automated drift detection runs every Monday at 6:00 AM UTC.

*This issue was automatically created by the infrastructure drift detection system.*