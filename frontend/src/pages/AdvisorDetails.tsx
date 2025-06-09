import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import advisorService from '../api/advisor.service';
import { Advisor } from '@/types';
import {
  Container,
  Typography,
  Grid,
  Paper,
  Box,
  Chip,
  Rating,
  Button,
  Divider,
  Card,
  CardContent,
  Avatar,
  CircularProgress,
  List,
  ListItem,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import {
  MailOutline,
  Language,
  EventAvailable,
  School,
  Work,
  AttachMoney
} from '@mui/icons-material';

const AdvisorDetail = () => {
  const { id } = useParams<{ id: string }>();
  const [advisor, setAdvisor] = useState<Advisor | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAdvisor = async () => {
      try {
        if (!id) return;
        const data = await advisorService.getAdvisorById(parseInt(id));
        console.log(data);
        setAdvisor(data);
      } catch (error) {
        toast.error('Failed to load advisor details');
        console.error('Advisor detail error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAdvisor();
  }, [id]);

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (!advisor) {
    return (
      <Container sx={{ mt: 4 }}>
        <Typography variant="h5">Advisor not found</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Paper sx={{ p: 3, mb: 4 }}>
        <Grid container spacing={4}>
          <Grid size={{ xs:12,md:4}}>
            <Box textAlign="center">
              <Avatar
                src={advisor.profileImageUrl || 'https://via.placeholder.com/300x300?text=Advisor'}
                alt={`${advisor.firstName} ${advisor.lastName}`}
                sx={{ width: 200, height: 200, mx: 'auto', mb: 2 }}
              />
              <Typography variant="h5" gutterBottom>
                {advisor.firstName} {advisor.lastName}
              </Typography>
              <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                {advisor.professionalTitle}
              </Typography>
              <Box display="flex" alignItems="center" justifyContent="center" mb={2}>
                <Rating value={advisor.averageRating} precision={0.5} readOnly />
                <Typography variant="body2" sx={{ ml: 1 }}>
                  ({advisor.ratingCount} reviews)
                </Typography>
              </Box>
              <Button
                variant="contained"
                color="primary"
                fullWidth
                component={Link}
                to={`/book-appointment/${advisor.id}`}
                sx={{ mb: 2 }}
              >
                Book Appointment
              </Button>
            </Box>
          </Grid>

          <Grid size={{xs:12,md:8}}>
            <List>
              <ListItem>
                <ListItemIcon>
                  <Work />
                </ListItemIcon>
                <ListItemText
                  primary="Experience"
                  secondary={`${advisor.experienceYears} years of experience as a financial advisor`}
                />
              </ListItem>

              <ListItem>
                <ListItemIcon>
                  <Language />
                </ListItemIcon>
                <ListItemText
                  primary="Languages"
                  secondary={
                    <Box display="flex" flexWrap="wrap" gap={0.5} mt={0.5}>
                      {advisor.languages.map(language => (
                        <Chip key={language} label={language} size="small" />
                      ))}
                    </Box>
                  }
                />
              </ListItem>
            </List>

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6" gutterBottom>
              About Me
            </Typography>
            <Typography variant="body1" paragraph>
              {advisor.bio}
            </Typography>

            <Typography variant="h6" gutterBottom>
              Specialties
            </Typography>
            <Box display="flex" flexWrap="wrap" gap={1} mb={3}>
              {advisor.specialties.map(specialty => (
                <Chip key={specialty} label={specialty} />
              ))}
            </Box>
          </Grid>
        </Grid>
      </Paper>

      <Typography variant="h5" gutterBottom>
        Book an Appointment
      </Typography>
      <Grid container spacing={3}>
        <Grid size={{xs:12,md:4}}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Initial Consultation
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                30-minute session to discuss your financial goals and how I can help.
              </Typography>
              <Typography variant="h6" color="primary" gutterBottom>
                Free
              </Typography>
              <Button
                variant="outlined"
                fullWidth
                component={Link}
                to={`/book-appointment/${advisor.id}?type=INITIAL_CONSULTATION`}
              >
                Book Now
              </Button>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{xs:12,md:4}} >
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Standard Session
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                60-minute comprehensive financial planning and advice session.
              </Typography>
              <Typography variant="h6" color="primary" gutterBottom>
                Free
              </Typography>
              <Button
                variant="outlined"
                fullWidth
                component={Link}
                to={`/book-appointment/${advisor.id}?type=STANDARD_SESSION`}
              >
                Book Now
              </Button>
            </CardContent>
          </Card>
        </Grid>

        <Grid  size={{xs:12,md:4}}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Financial Plan Review
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                60-minute session to review and adjust your existing financial plan.
              </Typography>
              <Typography variant="h6" color="primary" gutterBottom>
                Free
              </Typography>
              <Button
                variant="outlined"
                fullWidth
                component={Link}
                to={`/book-appointment/${advisor.id}?type=PLAN_REVIEW`}
              >
                Book Now
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default AdvisorDetail;