import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { toast } from 'react-toastify';
import appointmentService from '../api/appointment.service';
import { Appointment, AppointmentDetails } from '@/types';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  Grid,
  Divider,
  Chip,
  CircularProgress,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from '@mui/material';
import {
  Event,
  Person,
  AccessTime,
  Category,
  AttachMoney,
  VideoCall,
  Assignment
} from '@mui/icons-material';

const statusColors = {
  CONFIRMED: 'success',
  CANCELLED: 'error',
  COMPLETED: 'default'
};

const AppointmentDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState<AppointmentDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);

  useEffect(() => {
    const fetchAppointment = async () => {
      try {
        if (!id) return;
        const data = await appointmentService.getAppointmentById(parseInt(id));
        console.log(data);
        setAppointment(data);
      } catch (error) {
        toast.error('Failed to load appointment details');
        console.error('Appointment loading error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAppointment();
  }, [id]);

  const handleCancelAppointment = async () => {
    try {
      if (!id) return;
      await appointmentService.updateAppointmentStatus(parseInt(id), 'CANCELLED');
      toast.success('Appointment cancelled successfully');
      // Refresh appointment data
      const updatedAppointment = await appointmentService.getAppointmentById(parseInt(id));
      setAppointment(updatedAppointment);
      setCancelDialogOpen(false);
    } catch (error) {
      toast.error('Failed to cancel appointment');
      console.error('Cancel appointment error:', error);
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (!appointment) {
    return (
      <Container sx={{ mt: 4 }}>
        <Typography variant="h5">Appointment not found</Typography>
      </Container>
    );
  }

  const appointmentDate = new Date(appointment.appointmentDate);
  const isPastAppointment = appointmentDate < new Date();
  const canCancel = appointment.status === 'CONFIRMED' && !isPastAppointment;
  const canJoin = appointment.status === 'CONFIRMED' &&
    appointmentDate.getTime() - new Date().getTime() < 15 * 60 * 1000; // Within 15 minutes

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Appointment Details
      </Typography>

      <Paper sx={{ p: 3, mb: 4 }}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Event sx={{ mr: 1 }} />
                  <Typography variant="h6">
                    {format(appointmentDate, 'EEEE, MMMM d, yyyy')}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <AccessTime sx={{ mr: 1 }} />
                  <Typography variant="body1">
                    {format(appointmentDate, 'h:mm a')} -
                    {format(new Date(appointmentDate.getTime() + appointment.durationMinutes * 60000), 'h:mm a')}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Category sx={{ mr: 1 }} />
                  <Typography variant="body1">
                    {appointment.sessionType.replace(/_/g, ' ')}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <AttachMoney sx={{ mr: 1 }} />
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Typography variant="body1">
                      Free
                    </Typography>
                  </Box>
                </Box>
                <Box sx={{ mb: 2 }}>
                  <Chip
                    label={appointment.status}
                    color={statusColors[appointment.status as keyof typeof statusColors] as any}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12, md: 6 }}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Person sx={{ mr: 1 }} />
                  <Typography variant="h6">
                    {appointment.advisorFirstName} {appointment.advisorLastName}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  {appointment.advisorProfessionalTitle}
                </Typography>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {appointment.advisorSpecialties.slice(0, 3).map((specialty) => (
                    <Chip
                      key={specialty}
                      label={specialty}
                      size="small"
                      variant="outlined"
                    />
                  ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {appointment.meetingLink && (
          <Box mt={3}>
            <Button
              variant="contained"
              color="primary"
              fullWidth
              startIcon={<VideoCall />}
              disabled={!canJoin}
              component={Link}
              to={`/meeting/${appointment.id}`}
            >
              {canJoin ? 'Join Meeting' : 'Meeting link will be available 15 minutes before the appointment'}
            </Button>
          </Box>
        )}

        <Box mt={3} display="flex" gap={2} justifyContent="center">
          <Button
            variant="outlined"
            onClick={() => navigate(-1)}
          >
            Back
          </Button>

          {canCancel && (
            <Button
              variant="outlined"
              color="error"
              onClick={() => setCancelDialogOpen(true)}
            >
              Cancel Appointment
            </Button>
          )}
        </Box>
      </Paper>

      {appointment.sharedPlanId&& (
        <Paper sx={{ p: 3, mb: 4 }}>
          <Typography variant="h5" gutterBottom>
            Shared Financial Plan
          </Typography>
          <Box p={2} border={1} borderColor="divider" borderRadius={1}>
            <Typography variant="h6" gutterBottom>
              {appointment.sharedPlanName}
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Created on {appointment.sharedPlanCreationDate
              ? format(new Date(appointment.sharedPlanCreationDate), 'MMMM d, yyyy')
              : 'Date not available'
            }
            </Typography>
            <Divider sx={{ my: 2 }} />
            <Typography variant="body1" paragraph>
              {appointment.sharedPlanHealthAssessment}
            </Typography>
            <Button
              variant="outlined"
              component={Link}
              to={`/plan/${appointment.sharedPlanId}`}
            >
              View Full Plan
            </Button>
          </Box>
        </Paper>
      )}

      {appointment.userNotes && (
        <Paper sx={{ p: 3, mb: 4 }}>
          <Typography variant="h5" gutterBottom>
            Your Notes
          </Typography>
          <Typography variant="body1">
            {appointment.userNotes}
          </Typography>
        </Paper>
      )}

      {appointment.advisorNotes && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <Assignment sx={{ mr: 1 }} />
            <Typography variant="h5">
              Advisor Notes
            </Typography>
          </Box>
          <Typography variant="body1">
            {appointment.advisorNotes}
          </Typography>
        </Paper>
      )}

      {/* Confirmation Dialog for Cancel */}
      <Dialog
        open={cancelDialogOpen}
        onClose={() => setCancelDialogOpen(false)}
      >
        <DialogTitle>Cancel Appointment</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to cancel this appointment? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>No, Keep it</Button>
          <Button onClick={handleCancelAppointment} color="error">
            Yes, Cancel
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default AppointmentDetail;