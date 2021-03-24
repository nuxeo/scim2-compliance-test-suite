import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { Doughnut, defaults } from 'react-chartjs-2';
import { CardHeader } from '@material-ui/core';

defaults.global.animation.duration = 40000;

const useStyles = makeStyles({
  root: {
    width: '90%',
    // height: 650,
    borderRadius: 10,
    margin: 10,
    flex: 1,
  },
  media: {
    height: 140,
  },
});

var options = {
  legend: {
    position: 'bottom',
    labels: {
      boxWidth: 10,
      usePointStyle: true,
    },
  },
  maintainAspectRatio: false,
  cutoutPercentage: 65,
};

export default function Summary(props) {
  const classes = useStyles();
  const [data, setData] = React.useState({
    labels: ['Passed', 'Failed', 'skipped'],
    datasets: [
      {
        data: [
          props.statistics.success,
          props.statistics.failed,
          props.statistics.skipped,
        ],
        backgroundColor: ['#32CD32', '#bb3f3f', '#FFCE56'],
      },
    ],
  });

  return (
    // <Card style={{ height: 600, width: '90%' }}>
    //   <CardMedia>
    //     <Doughnut data={data} width={500} />
    //   </CardMedia>
    // </Card>
    <Card className={classes.root} elevation={2}>
      <CardHeader title="Summary" style={{ fontWeight: 1000 }} />
      <CardMedia>
        <Doughnut data={data} options={options} width={300} height={350} />
      </CardMedia>

      <CardContent>
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            // alignSelf: 'flex-start',
            //justifyContent: 'center',
            //alignContent: 'flex-start',
          }}
        >
          <Typography
            variant="body1"
            style={{ fontWeight: 400, align: 'center' }}
          >
            Total Results : {props.statistics.total}
          </Typography>
          <Typography
            variant="body1"
            style={{ fontWeight: 400, align: 'center' }}
          >
            Time : {props.statistics.time / 1000} s
          </Typography>
        </div>
      </CardContent>
    </Card>
  );
}
