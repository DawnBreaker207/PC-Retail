import dayjs from 'dayjs';

const formatDate = (date?: Date | string): string | undefined => {
  if (!date) return undefined;
  return dayjs(date).format('YYYY-MM-DD').toString();
};
const formatTime = (date?: Date | string): string | undefined => {
  if (!date) return undefined;
  return dayjs(date).format('HH:mm:ss').toString();
};
const timeFormat = (time: Date | string | null | undefined) => {
  if (!time) return '-';
  const today = dayjs().format('YYYY-MM-DD');
  const dateTime = dayjs(`${today} ${time}`, 'YYYY-MM-DD HH:mm:ss');
  if (!dateTime.isValid()) return '-';
  return dateTime.format('HH:mm');
};

export const DateHelper = { formatDate, formatTime, timeFormat };
