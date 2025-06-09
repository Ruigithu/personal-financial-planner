import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import advisorService from '../api/advisor.service';
import { Advisor } from '@/types';

import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Button,
  Box,
  Chip,
  Rating,
  TextField,
  InputAdornment,
  CircularProgress,
  Divider
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';

const AdvisorList = () => {
  const [advisors, setAdvisors] = useState<Advisor[]>([]);
  const [filteredAdvisors, setFilteredAdvisors] = useState<Advisor[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSpecialty, setSelectedSpecialty] = useState<string | null>(null);

  useEffect(() => {
    const fetchAdvisors = async () => {
      try {
        const data = await advisorService.getAllAdvisors();
        console.log(data);
        setAdvisors(data);
        setFilteredAdvisors(data);
      } catch (error) {
        toast.error('Failed to load advisors');
        console.error('Advisor loading error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAdvisors();
  }, []);

  useEffect(() => {
    let filtered = advisors;

    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(
        advisor =>
          (advisor.firstName || '').toLowerCase().includes(term) ||
          (advisor.lastName || '').toLowerCase().includes(term) ||
          (advisor.professionalTitle || '').toLowerCase().includes(term) ||
          (advisor.bio || '').toLowerCase().includes(term)
      );
    }

    // Filter by specialty
    if (selectedSpecialty) {
      filtered = filtered.filter(
        advisor => advisor.specialties.includes(selectedSpecialty)
      );
    }

    setFilteredAdvisors(filtered);
  }, [advisors, searchTerm, selectedSpecialty]);

  // Get unique specialties from all advisors
  const allSpecialties = advisors.reduce((specialties, advisor) => {
    advisor.specialties.forEach(specialty => {
      if (!specialties.includes(specialty)) {
        specialties.push(specialty);
      }
    });
    return specialties;
  }, [] as string[]);

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Find a Financial Advisor
      </Typography>

      <Box mb={4}>
        <TextField
          fullWidth
          placeholder="Search by name, title, or bio"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
          sx={{ mb: 2 }}
        />

        <Box display="flex" flexWrap="wrap" gap={1}>
          {allSpecialties.map(specialty => (
            <Chip
              key={specialty}
              label={specialty}
              clickable
              color={selectedSpecialty === specialty ? 'primary' : 'default'}
              onClick={() =>
                setSelectedSpecialty(selectedSpecialty === specialty ? null : specialty)
              }
            />
          ))}
        </Box>
      </Box>

      {filteredAdvisors.length === 0 ? (
        <Typography variant="body1">
          No advisors found matching your criteria.
        </Typography>
      ) : (
        <Grid container spacing={3}>
          {filteredAdvisors.map((advisor) => (
            <Grid size={{xs:12,sm:6,md:4}} key={advisor.id}  >
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardMedia
                  component="img"
                  height="200"
                  image={advisor.profileImageUrl || 'https://via.placeholder.com/300x200?text=Advisor'}
                  alt={`${advisor.firstName} ${advisor.lastName}`}
                />
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography gutterBottom variant="h5" component="div">
                    {advisor.firstName} {advisor.lastName}
                  </Typography>
                  <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                    {advisor.professionalTitle}
                  </Typography>
                  <Box display="flex" alignItems="center" mb={1}>
                    <Rating value={advisor.averageRating} precision={0.5} readOnly />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      ({advisor.ratingCount})
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {advisor.experienceYears} years experience
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    ${advisor.hourlyRate}/hour
                  </Typography>
                  <Divider sx={{ my: 1 }} />
                  <Typography variant="body2" noWrap>
                    {advisor.bio}
                  </Typography>
                  <Box mt={2} display="flex" flexWrap="wrap" gap={0.5}>
                    {advisor.specialties.slice(0, 3).map((specialty) => (
                      <Chip
                        key={specialty}
                        label={specialty}
                        size="small"
                        variant="outlined"
                      />
                    ))}
                    {advisor.specialties.length > 3 && (
                      <Chip
                        label={`+${advisor.specialties.length - 3}`}
                        size="small"
                        variant="outlined"
                      />
                    )}
                  </Box>
                </CardContent>
                <CardActions>
                  <Button
                    size="small"
                    component={Link}
                    to={`/advisor/${advisor.id}`}
                  >
                    View Profile
                  </Button>
                    <Button
                    size="small"
                    component={Link}
                    to={`/book-appointment/${advisor.id}`}
                    variant="contained"
                    color="primary"
                  >
                    Book Appointment
                  </Button>
                </CardActions>
              </Card>
            </Grid>

          ))}
        </Grid>
      )}
    </Container>
  );
};

export default AdvisorList;