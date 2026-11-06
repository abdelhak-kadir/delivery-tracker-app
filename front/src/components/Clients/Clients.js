// src/components/Clients/Clients.js
import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Button,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Snackbar,
  Card,
  CardContent,
  Avatar,
  Paper
} from '@mui/material';
import {
  Add as AddIcon,
  Business as BusinessIcon,
  Badge as BadgeIcon
} from '@mui/icons-material';
import { clientsAPI } from '../../services/api';
import DataTable from '../Common/DataTable';
import LoadingSpinner from '../Common/LoadingSpinner';
import ConfirmDialog from '../Common/ConfirmDialog';

const Clients = () => {
  // State
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  
  // Dialog states
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingClient, setEditingClient] = useState(null);
  const [formData, setFormData] = useState({ name: '', identityId: '' });
  const [formErrors, setFormErrors] = useState({});
  
  // Delete confirmation
  const [deleteDialog, setDeleteDialog] = useState({ open: false, client: null });
  
  // Notification
  const [notification, setNotification] = useState({ open: false, message: '', severity: 'success' });

  // Table columns
  const columns = [
    { 
      id: 'id', 
      label: 'ID',
      render: (row) => `#${row.id}`
    },
    { 
      id: 'name', 
      label: 'Client Name',
      render: (row) => (
        <Box display="flex" alignItems="center" gap={2}>
          <Avatar sx={{ bgcolor: 'primary.main' }}>
            {row.name.charAt(0).toUpperCase()}
          </Avatar>
          <Typography variant="body2" fontWeight={600}>
            {row.name}
          </Typography>
        </Box>
      )
    },
    { 
      id: 'identityId', 
      label: 'Identity ID',
      render: (row) => (
        <Box display="flex" alignItems="center" gap={1}>
          <BadgeIcon fontSize="small" color="action" />
          <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
            {row.identityId}
          </Typography>
        </Box>
      )
    }
  ];

  // Show notification
  const showNotification = useCallback((message, severity = 'success') => {
    setNotification({ open: true, message, severity });
  }, []);

  const fetchClients = useCallback(async () => {
    try {
      setLoading(true);
      const response = await clientsAPI.getAll(page, size);
      const { data, totalElements: total } = response.data.data;
      setClients(data || []);
      setTotalElements(total || 0);
    } catch (error) {
      showNotification('Error fetching clients', 'error');
    } finally {
      setLoading(false);
    }
  }, [page, size, showNotification]);

  useEffect(() => {
    fetchClients();
  }, [fetchClients]);

  // Dialog handlers
  const handleCreate = () => {
    setEditingClient(null);
    setFormData({ name: '', identityId: '' });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleEdit = (client) => {
    setEditingClient(client);
    setFormData({ name: client.name, identityId: client.identityId });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleDelete = (client) => {
    setDeleteDialog({ open: true, client });
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
  };

  // Form validation
  const validateForm = () => {
    const errors = {};
    
    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    }
    
    if (!formData.identityId.trim()) {
      errors.identityId = 'Identity ID is required';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Form submit
  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      if (editingClient) {
        await clientsAPI.update({ id: editingClient.id, ...formData });
        showNotification('Client updated successfully');
      } else {
        await clientsAPI.create(formData);
        showNotification('Client created successfully');
      }
      
      setDialogOpen(false);
      fetchClients();
    } catch (error) {
      showNotification('Error saving client', 'error');
    }
  };

  // Delete client
  const confirmDelete = async () => {
    try {
      await clientsAPI.delete(deleteDialog.client.id);
      showNotification('Client deleted successfully');
      setDeleteDialog({ open: false, client: null });
      fetchClients();
    } catch (error) {
      showNotification('Error deleting client', 'error');
    }
  };

  // Form change handler
  const handleFormChange = (field, value) => {
    setFormData({ ...formData, [field]: value });
    if (formErrors[field]) {
      setFormErrors({ ...formErrors, [field]: null });
    }
  };

  return (
    <Box>
      {/* Header */}
      <Paper 
        sx={{ 
          p: 3, 
          mb: 3, 
          bgcolor: 'primary.main',
          color: 'white',
          borderRadius: 2
        }}
      >
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h4" fontWeight="bold" gutterBottom>
              Clients Management
            </Typography>
            <Typography variant="body1">
              Manage your client database
            </Typography>
          </Box>
          <Button
            variant="contained"
            size="large"
            startIcon={<AddIcon />}
            onClick={handleCreate}
            sx={{
              bgcolor: 'white',
              color: 'primary.main',
              '&:hover': { bgcolor: 'grey.100' }
            }}
          >
            Add Client
          </Button>
        </Box>
      </Paper>

      {/* Stats */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box>
              <Typography variant="h3" fontWeight="bold" color="primary">
                {totalElements}
              </Typography>
              <Typography variant="h6" color="text.secondary">
                Total Clients
              </Typography>
            </Box>
            <Avatar sx={{ bgcolor: 'primary.light', width: 60, height: 60 }}>
              <BusinessIcon sx={{ fontSize: 30 }} />
            </Avatar>
          </Box>
        </CardContent>
      </Card>

      {/* Data Table */}
      <Card>
        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
            <LoadingSpinner />
          </Box>
        ) : (
          <DataTable
            data={clients}
            columns={columns}
            totalElements={totalElements}
            page={page}
            size={size}
            onPageChange={setPage}
            onRowsPerPageChange={setSize}
            onEdit={handleEdit}
            onDelete={handleDelete}
          />
        )}
      </Card>

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingClient ? 'Edit Client' : 'Add New Client'}
        </DialogTitle>
        
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <TextField
              autoFocus
              label="Client Name"
              fullWidth
              variant="outlined"
              value={formData.name}
              onChange={(e) => handleFormChange('name', e.target.value)}
              error={!!formErrors.name}
              helperText={formErrors.name}
              sx={{ mb: 3 }}
            />
            
            <TextField
              label="Identity ID"
              fullWidth
              variant="outlined"
              value={formData.identityId}
              onChange={(e) => handleFormChange('identityId', e.target.value)}
              error={!!formErrors.identityId}
              helperText={formErrors.identityId}
            />
          </Box>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={handleCloseDialog}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} variant="contained">
            {editingClient ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, client: null })}
        onConfirm={confirmDelete}
        title="Delete Client"
        message={`Are you sure you want to delete "${deleteDialog.client?.name}"?`}
        confirmText="Delete"
        severity="error"
      />

      {/* Notification */}
      <Snackbar
        open={notification.open}
        autoHideDuration={6000}
        onClose={() => setNotification({ ...notification, open: false })}
      >
        <Alert
          onClose={() => setNotification({ ...notification, open: false })}
          severity={notification.severity}
          variant="filled"
        >
          {notification.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default Clients;