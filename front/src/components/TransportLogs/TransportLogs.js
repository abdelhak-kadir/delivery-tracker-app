// src/components/TransportLogs/TransportLogs.js
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
  Grid,
  Autocomplete,
  Card,
  CardContent,
  Avatar,
  Paper
} from '@mui/material';
import {
  Add as AddIcon,
  LocalShipping as TruckIcon,
  Person as PersonIcon,
  Business as BusinessIcon,

} from '@mui/icons-material';
import { transportLogsAPI, clientsAPI, driversAPI } from '../../services/api';
import DataTable from '../Common/DataTable';
import LoadingSpinner from '../Common/LoadingSpinner';
import ConfirmDialog from '../Common/ConfirmDialog';

const TransportLogs = () => {
  // State
  const [logs, setLogs] = useState([]);
  const [clients, setClients] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  
  // Dialog states
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingLog, setEditingLog] = useState(null);
  const [formData, setFormData] = useState({
    clientId: null,
    driverId: null,
    loadDate: '',
    loadLocation: '',
    unloadDate: '',
    unloadLocation: '',
    destinationName: '',
    deliveryNote: '',
    advance: '',
    fuelQuantity: '',
    fuelPricePerLiter: '',
    variableCharge: '',
    chargePrice: '',
    clientTariff: '',
    tripPrice: '',
    operator: '',
    commercial: ''
  });
  const [formErrors, setFormErrors] = useState({});
  
  // Delete confirmation
  const [deleteDialog, setDeleteDialog] = useState({ open: false, log: null });
  
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
      id: 'client', 
      label: 'Client', 
      render: (row) => (
        <Box display="flex" alignItems="center" gap={1}>
          <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
            <BusinessIcon fontSize="small" />
          </Avatar>
          <Typography variant="body2">
            {row.client?.name || 'N/A'}
          </Typography>
        </Box>
      )
    },
    { 
      id: 'driver', 
      label: 'Driver', 
      render: (row) => (
        <Box display="flex" alignItems="center" gap={1}>
          <Avatar sx={{ width: 32, height: 32, bgcolor: 'warning.main' }}>
            <PersonIcon fontSize="small" />
          </Avatar>
          <Box>
            <Typography variant="body2">
              {row.driver?.name || 'N/A'}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {row.driver?.plateNumber}
            </Typography>
          </Box>
        </Box>
      )
    },
    { 
      id: 'destinationName', 
      label: 'Destination',
      render: (row) => row.destinationName
    },
    { 
      id: 'loadDate', 
      label: 'Load Date', 
      render: (row) => new Date(row.loadDate).toLocaleDateString()
    },
    { 
      id: 'tripPrice', 
      label: 'Trip Price', 
      render: (row) => (
        <Typography variant="body2" fontWeight={600} color="success.main">
          ${parseFloat(row.tripPrice).toFixed(2)}
        </Typography>
      )
    }
  ];

  // Show notification
  const showNotification = useCallback((message, severity = 'success') => {
    setNotification({ open: true, message, severity });
  }, []);

  const fetchInitialData = useCallback(async () => {
    try {
      const [clientsRes, driversRes] = await Promise.all([
        clientsAPI.getAll(0, 100),
        driversAPI.getAll(0, 100)
      ]);
      
      setClients(clientsRes.data.data.data || []);
      setDrivers(driversRes.data.data.data || []);
    } catch (error) {
      showNotification('Error fetching clients and drivers', 'error');
    }
  }, [showNotification]);

  useEffect(() => {
    fetchInitialData();
  }, [fetchInitialData]);

  const fetchLogs = useCallback(async () => {
    try {
      setLoading(true);
      const response = await transportLogsAPI.getAll(page, size);
      const { data, totalElements: total } = response.data.data;
      setLogs(data || []);
      setTotalElements(total || 0);
    } catch (error) {
      showNotification('Error fetching transport logs', 'error');
    } finally {
      setLoading(false);
    }
  }, [page, size, showNotification]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  // Dialog handlers
  const handleCreate = () => {
    setEditingLog(null);
    setFormData({
      clientId: null,
      driverId: null,
      loadDate: '',
      loadLocation: '',
      unloadDate: '',
      unloadLocation: '',
      destinationName: '',
      deliveryNote: '',
      advance: '',
      fuelQuantity: '',
      fuelPricePerLiter: '',
      variableCharge: '',
      chargePrice: '',
      clientTariff: '',
      tripPrice: '',
      operator: '',
      commercial: ''
    });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleEdit = (log) => {
    setEditingLog(log);
    setFormData({
      clientId: log.client?.id || null,
      driverId: log.driver?.id || null,
      loadDate: log.loadDate,
      loadLocation: log.loadLocation,
      unloadDate: log.unloadDate,
      unloadLocation: log.unloadLocation,
      destinationName: log.destinationName,
      deliveryNote: log.deliveryNote || '',
      advance: log.advance.toString(),
      fuelQuantity: log.fuelQuantity.toString(),
      fuelPricePerLiter: log.fuelPricePerLiter.toString(),
      variableCharge: log.variableCharge.toString(),
      chargePrice: log.chargePrice.toString(),
      clientTariff: log.clientTariff.toString(),
      tripPrice: log.tripPrice.toString(),
      operator: log.operator,
      commercial: log.commercial
    });
    setFormErrors({});
    setDialogOpen(true);
  };

  const handleDelete = (log) => {
    setDeleteDialog({ open: true, log });
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
  };

  // Form validation
  const validateForm = () => {
    const errors = {};
    
    if (!formData.clientId) errors.clientId = 'Client is required';
    if (!formData.driverId) errors.driverId = 'Driver is required';
    if (!formData.loadDate) errors.loadDate = 'Load date is required';
    if (!formData.loadLocation.trim()) errors.loadLocation = 'Load location is required';
    if (!formData.unloadDate) errors.unloadDate = 'Unload date is required';
    if (!formData.unloadLocation.trim()) errors.unloadLocation = 'Unload location is required';
    if (!formData.destinationName.trim()) errors.destinationName = 'Destination name is required';
    if (!formData.operator.trim()) errors.operator = 'Operator is required';
    if (!formData.commercial.trim()) errors.commercial = 'Commercial is required';
    
    // Validate numeric fields
    const numericFields = ['advance', 'fuelQuantity', 'fuelPricePerLiter', 'variableCharge', 'chargePrice', 'clientTariff', 'tripPrice'];
    numericFields.forEach(field => {
      if (!formData[field] || isNaN(formData[field])) {
        errors[field] = `${field} must be a valid number`;
      }
    });
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Form submit
  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      const submitData = {
        ...formData,
        advance: parseFloat(formData.advance),
        fuelQuantity: parseFloat(formData.fuelQuantity),
        fuelPricePerLiter: parseFloat(formData.fuelPricePerLiter),
        variableCharge: parseFloat(formData.variableCharge),
        chargePrice: parseFloat(formData.chargePrice),
        clientTariff: parseFloat(formData.clientTariff),
        tripPrice: parseFloat(formData.tripPrice)
      };

      if (editingLog) {
        await transportLogsAPI.update({ id: editingLog.id, ...submitData });
        showNotification('Transport log updated successfully');
      } else {
        await transportLogsAPI.create(submitData);
        showNotification('Transport log created successfully');
      }
      
      setDialogOpen(false);
      fetchLogs();
    } catch (error) {
      showNotification('Error saving transport log', 'error');
    }
  };

  // Delete log
  const confirmDelete = async () => {
    try {
      await transportLogsAPI.delete(deleteDialog.log.id);
      showNotification('Transport log deleted successfully');
      setDeleteDialog({ open: false, log: null });
      fetchLogs();
    } catch (error) {
      showNotification('Error deleting transport log', 'error');
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
          bgcolor: 'info.main',
          color: 'white',
          borderRadius: 2
        }}
      >
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h4" fontWeight="bold" gutterBottom>
              Transport Logs Management
            </Typography>
            <Typography variant="body1">
              Manage and track all transport operations
            </Typography>
          </Box>
          <Button
            variant="contained"
            size="large"
            startIcon={<AddIcon />}
            onClick={handleCreate}
            sx={{
              bgcolor: 'white',
              color: 'info.main',
              '&:hover': { bgcolor: 'grey.100' }
            }}
          >
            Add Transport Log
          </Button>
        </Box>
      </Paper>

      {/* Stats */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h3" fontWeight="bold" color="info.main">
                    {totalElements}
                  </Typography>
                  <Typography variant="h6" color="text.secondary">
                    Total Logs
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: 'info.light', width: 60, height: 60 }}>
                  <TruckIcon sx={{ fontSize: 30 }} />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h3" fontWeight="bold" color="success.main">
                    {clients.length}
                  </Typography>
                  <Typography variant="h6" color="text.secondary">
                    Active Clients
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: 'success.light', width: 60, height: 60 }}>
                  <BusinessIcon sx={{ fontSize: 30 }} />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h3" fontWeight="bold" color="warning.main">
                    {drivers.length}
                  </Typography>
                  <Typography variant="h6" color="text.secondary">
                    Active Drivers
                  </Typography>
                </Box>
                <Avatar sx={{ bgcolor: 'warning.light', width: 60, height: 60 }}>
                  <PersonIcon sx={{ fontSize: 30 }} />
                </Avatar>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Data Table */}
      <Card>
        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
            <LoadingSpinner />
          </Box>
        ) : (
          <DataTable
            data={logs}
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
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingLog ? 'Edit Transport Log' : 'Add New Transport Log'}
        </DialogTitle>
        
        <DialogContent>
          <Grid container spacing={3} sx={{ mt: 1 }}>
            {/* Client and Driver */}
            <Grid item xs={12} md={6}>
              <Autocomplete
                options={clients}
                getOptionLabel={(option) => option.name}
                value={clients.find(c => c.id === formData.clientId) || null}
                onChange={(event, newValue) => handleFormChange('clientId', newValue?.id || null)}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Client"
                    error={!!formErrors.clientId}
                    helperText={formErrors.clientId}
                  />
                )}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <Autocomplete
                options={drivers}
                getOptionLabel={(option) => `${option.name} (${option.plateNumber})`}
                value={drivers.find(d => d.id === formData.driverId) || null}
                onChange={(event, newValue) => handleFormChange('driverId', newValue?.id || null)}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Driver"
                    error={!!formErrors.driverId}
                    helperText={formErrors.driverId}
                  />
                )}
              />
            </Grid>

            {/* Dates */}
            <Grid item xs={12} md={6}>
              <TextField
                label="Load Date"
                type="date"
                fullWidth
                value={formData.loadDate}
                onChange={(e) => handleFormChange('loadDate', e.target.value)}
                error={!!formErrors.loadDate}
                helperText={formErrors.loadDate}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                label="Unload Date"
                type="date"
                fullWidth
                value={formData.unloadDate}
                onChange={(e) => handleFormChange('unloadDate', e.target.value)}
                error={!!formErrors.unloadDate}
                helperText={formErrors.unloadDate}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            {/* Locations */}
            <Grid item xs={12} md={6}>
              <TextField
                label="Load Location"
                fullWidth
                value={formData.loadLocation}
                onChange={(e) => handleFormChange('loadLocation', e.target.value)}
                error={!!formErrors.loadLocation}
                helperText={formErrors.loadLocation}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                label="Unload Location"
                fullWidth
                value={formData.unloadLocation}
                onChange={(e) => handleFormChange('unloadLocation', e.target.value)}
                error={!!formErrors.unloadLocation}
                helperText={formErrors.unloadLocation}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                label="Destination Name"
                fullWidth
                value={formData.destinationName}
                onChange={(e) => handleFormChange('destinationName', e.target.value)}
                error={!!formErrors.destinationName}
                helperText={formErrors.destinationName}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                label="Delivery Note"
                fullWidth
                multiline
                rows={3}
                value={formData.deliveryNote}
                onChange={(e) => handleFormChange('deliveryNote', e.target.value)}
              />
            </Grid>

            {/* Financial fields - simplified layout */}
            <Grid item xs={12} md={6}>
              <TextField
                label="Advance"
                type="number"
                fullWidth
                value={formData.advance}
                onChange={(e) => handleFormChange('advance', e.target.value)}
                error={!!formErrors.advance}
                helperText={formErrors.advance}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                label="Trip Price"
                type="number"
                fullWidth
                value={formData.tripPrice}
                onChange={(e) => handleFormChange('tripPrice', e.target.value)}
                error={!!formErrors.tripPrice}
                helperText={formErrors.tripPrice}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                label="Fuel Quantity (L)"
                type="number"
                fullWidth
                value={formData.fuelQuantity}
                onChange={(e) => handleFormChange('fuelQuantity', e.target.value)}
                error={!!formErrors.fuelQuantity}
                helperText={formErrors.fuelQuantity}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                label="Fuel Price Per Liter"
                type="number"
                fullWidth
                value={formData.fuelPricePerLiter}
                onChange={(e) => handleFormChange('fuelPricePerLiter', e.target.value)}
                error={!!formErrors.fuelPricePerLiter}
                helperText={formErrors.fuelPricePerLiter}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                label="Variable Charge"
                type="number"
                fullWidth
                value={formData.variableCharge}
                onChange={(e) => handleFormChange('variableCharge', e.target.value)}
                error={!!formErrors.variableCharge}
                helperText={formErrors.variableCharge}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                label="Charge Price"
                type="number"
                fullWidth
                value={formData.chargePrice}
                onChange={(e) => handleFormChange('chargePrice', e.target.value)}
                error={!!formErrors.chargePrice}
                helperText={formErrors.chargePrice}
              />
            </Grid>
            
            <Grid item xs={12}>
              <TextField
                label="Client Tariff"
                type="number"
                fullWidth
                value={formData.clientTariff}
                onChange={(e) => handleFormChange('clientTariff', e.target.value)}
                error={!!formErrors.clientTariff}
                helperText={formErrors.clientTariff}
              />
            </Grid>

            {/* Personnel */}
            <Grid item xs={12} md={6}>
              <TextField
                label="Operator"
                fullWidth
                value={formData.operator}
                onChange={(e) => handleFormChange('operator', e.target.value)}
                error={!!formErrors.operator}
                helperText={formErrors.operator}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                label="Commercial"
                fullWidth
                value={formData.commercial}
                onChange={(e) => handleFormChange('commercial', e.target.value)}
                error={!!formErrors.commercial}
                helperText={formErrors.commercial}
              />
            </Grid>
          </Grid>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={handleCloseDialog}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} variant="contained" color="info">
            {editingLog ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation */}
      <ConfirmDialog
        open={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, log: null })}
        onConfirm={confirmDelete}
        title="Delete Transport Log"
        message={`Are you sure you want to delete transport log #${deleteDialog.log?.id}?`}
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

export default TransportLogs;



