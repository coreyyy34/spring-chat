export const isToday = (date) => {
    const now = new Date();
    return (
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth() &&
        date.getDate() === now.getDate()
    );
}

export const isYesterday = (date) => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    return (
        date.getFullYear() === yesterday.getFullYear() &&
        date.getMonth() === yesterday.getMonth() &&
        date.getDate() === yesterday.getDate()
    );
}

export const formatTime = (date) => {
    return new Intl.DateTimeFormat("en-NZ", {
        hour: "numeric",
        minute: "2-digit",
        hour12: true,
    }).format(date);
}

export const formatShortDate = (date) => {
    return new Intl.DateTimeFormat("en-NZ", {
        year: "2-digit",
        month: "2-digit",
        day: "2-digit",
    }).format(date);
}

export const formatMonthDayYear = (date) => {
    return new Intl.DateTimeFormat("en-NZ", {
        year: "numeric",
        month: "long",
        day: "numeric",
    }).format(date);
}
