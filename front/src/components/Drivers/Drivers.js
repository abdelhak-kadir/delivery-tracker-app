// src/components/Drivers/Drivers.js
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
  LocalShipping as TruckIcon,
  Badge as PlateIcon
} from '@mui/icons-material';
import { driversAPI } from '../../services/api';
import DataTable from '../Common/DataTable';
import LoadingSpinner from '../Common/LoadingSpinner';
import ConfirmDialog from '../Common/ConfirmDialog';

const Drivers = () => {
  // State
  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  
  // Dialog states
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingDriver, setEditingDriver] = useState(null);
  const [formData, setFormData] = useState({ name: '', plateNumber: '' });
  const [formErrors, setFormErrors] = useState({});
  
  // Delete confirmation
  const [deleteDialog, setDeleteDialog] = useState({ open: false, driver: null });
  
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
      label: 'Driver Name',
      render: (row) => (
        <Box display="flex" alignItems="center" gap={2}>
          <Avatar sx={{ bgcolor: 'warning.main' }}>
            {row.name.charAt(0).toUpperCase()}
          </Avatar>
          <Typography variant="body2" fontWeight={600}>
            {row.name}
          </Typography>
        </Box>
      )
    },
    { 
      id: 'plateNumber', 
      label: 'Plate Number',
      render: (row) => (
        <Box display="flex" alignItems="center" gap={1}>
          <PlateIcon fontSize="small" color="action" />
          <Typography 
            variant="body2" 
            sx={{ 
              fontFamily: 'monospace',
              fontWeight: 'bold',
              bgcolor: 'grey.100',
              px: 1,
              py: 0.5,
              borderRadius: 1
            }}
          >
            {row.plateNumber}
          </Typography>
        </Box>
      )
    }
  ];

  // Show notification
  const showNotification = useCallback((message, severity = 'success') => {
    setNotification({ open: true, message, severity });
  }, []);

  const fetchDrivers = useCallback(async () => {
    try {
      setLoading(true);
      const response = await driversAPI.getAll(page, size);
      const { data, totalElements: total } = response.data.data;
      setDrivers(data || []);
      setTotalElements(total || 0);
    } catch (error) {
      showNotification('Error fetching drivers', 'error');
    } finally {
      setLoading(false);
    }
  }, [page, size, showNotification]);

  useEffect(() => {
    fetchDrivers();
  }, [fetchDrivers]);

  // Dialog handlers
  const handleCreate = () => {
    setEditingDriver(null);
    setFormData({ name: '', plateNumber: '' });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleEdit = (driver) => {
    setEditingDriver(driver);
    setFormData({ name: driver.name, plateNumber: driver.plateNumber });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleDelete = (driver) => {
    setDeleteDialog({ open: true, driver });
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
  };

  // Form validation
  const validateForm = () => {
    const errors = {};
    
    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    } else if (formData.name.length > 50) {
      errors.name = 'Name must be less than 50 characters';
    }
    
    if (!formData.plateNumber.trim()) {
      errors.plateNumber = 'Plate Number is required';
    } else if (formData.plateNumber.length > 20) {
      errors.plateNumber = 'Plate Number must be less than 20 characters';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Form submit
  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      if (editingDriver) {
        await driversAPI.update({ id: editingDriver.id, ...formData });
        showNotification('Driver updated successfully');
      } else {
        await driversAPI.create(formData);
        showNotification('Driver created successfully');
      }
      
      setDialogOpen(false);
      fetchDrivers();
    } catch (error) {
      showNotification('Error saving driver', 'error');
    }
  };

  // Delete driver
  const confirmDelete = async () => {
    try {
      await driversAPI.delete(deleteDialog.driver.id);
      showNotification('Driver deleted successfully');
      setDeleteDialog({ open: false, driver: null });
      fetchDrivers();
    } catch (error) {
      showNotification('Error deleting driver', 'error');
    }
  };

  // Form change handler
  const handleFormChange = (field, value) => {
    // Auto-uppercase plate numbers
    if (field === 'plateNumber') {
      value = value.toUpperCase();
    }
    
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
          bgcolor: 'warning.main',
          color: 'white',
          borderRadius: 2
        }}
      >
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h4" fontWeight="bold" gutterBottom>
              Drivers Management
            </Typography>
            <Typography variant="body1">
              Manage your driver fleet
            </Typography>
          </Box>
          <Button
            variant="contained"
            size="large"
            startIcon={<AddIcon />}
            onClick={handleCreate}
            sx={{
              bgcolor: 'white',
              color: 'warning.main',
              '&:hover': { bgcolor: 'grey.100' }
            }}
          >
            Add Driver
          </Button>
        </Box>
      </Paper>

      {/* Stats */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Box>
              <Typography variant="h3" fontWeight="bold" color="warning.main">
                {totalElements}
              </Typography>
              <Typography variant="h6" color="text.secondary">
                Total Drivers
              </Typography>
            </Box>
            <Avatar sx={{ bgcolor: 'warning.light', width: 60, height: 60 }}>
              <TruckIcon sx={{ fontSize: 30 }} />
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
            data={drivers}
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
          {editingDriver ? 'Edit Driver' : 'Add New Driver'}
        </DialogTitle>
        
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <TextField
              autoFocus
              label="Driver Name"
              fullWidth
              variant="outlined"
              value={formData.name}
              onChange={(e) => handleFormChange('name', e.target.value)}
              error={!!formErrors.name}
              helperText={formErrors.name}
              sx={{ mb: 3 }}
              placeholder="Enter driver's full name"
            />
            
            <TextField
              label="Plate Number"
              fullWidth
              variant="outlined"
              value={formData.plateNumber}
              onChange={(e) => handleFormChange('plateNumber', e.target.value)}
              error={!!formErrors.plateNumber}
              helperText={formErrors.plateNumber}
              placeholder="ABC-123"
              inputProps={{ 
                style: { fontFamily: 'monospace', textTransform: 'uppercase' } 
              }}
            />
          </Box>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={handleCloseDialog}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} variant="contained" color="warning">
            {editingDriver ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, driver: null })}
        onConfirm={confirmDelete}
        title="Delete Driver"
        message={`Are you sure you want to delete "${deleteDialog.driver?.name}" with plate number "${deleteDialog.driver?.plateNumber}"?`}
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

export default Drivers;